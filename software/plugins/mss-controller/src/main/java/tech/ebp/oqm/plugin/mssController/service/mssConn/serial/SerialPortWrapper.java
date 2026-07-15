package tech.ebp.oqm.plugin.mssController.service.mssConn.serial;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortInvalidPortException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.mssController.model.exception.MssCommandTimeoutException;
import tech.ebp.oqm.plugin.mssController.model.exception.SerialModuleLockRequiredException;
import tech.ebp.oqm.plugin.mssController.model.exception.SerialPortClosedException;
import tech.ebp.oqm.plugin.mssController.model.exception.SerialPortSetupFailedException;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe wrapper around a serial port for JSON communication with an MSS module.
 * <p>
 * Provides mutual exclusion via {@link ReentrantLock}, enforces a minimum spacing
 * between messages, and handles blocking timeouts. Use {@link #startComm()} to begin
 * a locked transaction, then {@link #write(Object)} or {@link #readJson()} inside it.
 */
@Getter(AccessLevel.PRIVATE)
@Slf4j
public class SerialPortWrapper implements AutoCloseable {

	private final ReentrantLock lock = new ReentrantLock();
	private final ObjectMapper objectMapper;
	private final Duration commSpacing;
	private final Duration commandResponseTimeout;
	private final SerialPort port;

	private ZonedDateTime noCommBefore = null;


	/** Opens the port and configures timeouts. */
	public SerialPortWrapper(
		ObjectMapper objectMapper,
		String portPath,
		Optional<Integer> baudRate,
		Duration commSpacing,
		Duration readTimeout,
		Duration writeTimeout,
		Duration commandResponseTimeout
	) throws SerialPortSetupFailedException {
		log.info("Setting up connection to MSS serial port: {}", portPath);
		this.objectMapper = objectMapper;
		this.commSpacing = commSpacing;
		this.commandResponseTimeout = commandResponseTimeout;

		SerialPort newPort;
		try{
			newPort = SerialPort.getCommPort(portPath);
		} catch (SerialPortInvalidPortException e) {
			throw new SerialPortSetupFailedException(portPath, e);
		}

		baudRate.ifPresent(newPort::setBaudRate);

		newPort.addDataListener(new SerialPortDataListener() {
			@Override
			public int getListeningEvents() {
				return SerialPort.LISTENING_EVENT_PORT_DISCONNECTED;
			}

			@Override
			public void serialEvent(SerialPortEvent serialPortEvent) {
				if (serialPortEvent.getEventType() == SerialPort.LISTENING_EVENT_PORT_DISCONNECTED) {
					newPort.closePort();
				}
			}
		});

		if (!newPort.openPort((int) commSpacing.toMillis())) {
			throw new SerialPortSetupFailedException(portPath);
		}

		newPort.setComPortTimeouts(
			SerialPort.TIMEOUT_READ_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING,
			(int) readTimeout.toMillis(),
			(int) writeTimeout.toMillis()
		);

		log.info("Connection to MSS serial port setup: {}", portPath);

		this.port = newPort;
	}

	/** Updates the comm-spacing deadline to now + configured spacing. */
	public void updateNoCommBefore() {
		this.noCommBefore = ZonedDateTime.now().plus(this.getCommSpacing());
	}

	/** Returns true if the minimum inter-message spacing has elapsed. */
	public boolean pastCommSpacing() {
		if(this.getNoCommBefore() == null){
			return true;
		}
		return !ZonedDateTime.now().isBefore(this.getNoCommBefore());
	}

	/** Blocks until the minimum inter-message spacing has elapsed. */
	public void waitForCommSpacing() {
		if (this.noCommBefore == null) {
			return;
		}
		ZonedDateTime now = ZonedDateTime.now();

		if (!this.pastCommSpacing()) {
			Duration waitTime = Duration.between(
				now,
				this.getNoCommBefore()
			);
			try {
				Thread.sleep(waitTime.toMillis() + 1); //plus one to ensure clear threshold
			} catch(InterruptedException e) {
				throw new RuntimeException("Failed to wait for comm spacing.", e);
			}
		}
	}


	/** Asserts the current thread holds the serial lock; throws otherwise. */
	public void assertLockAcquired() throws SerialModuleLockRequiredException {
		if (!this.getLock().isHeldByCurrentThread()) {
			throw new SerialModuleLockRequiredException();
		}
	}

	/**
	 * Scoped lock token for a serial transaction.
	 * <p>
	 * Use in a try-with-resources block; {@link #close()} releases the lock
	 * and updates the comm-spacing deadline. Prefer {@link #startComm()} to acquire one.
	 */
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class CommAction implements AutoCloseable {

		private final SerialPortWrapper wrapper;

		@Override
		public void close() {
			this.wrapper.getLock().unlock();
			this.wrapper.updateNoCommBefore();
		}
	}

	/** Acquires the serial lock and returns a scoped {@link CommAction} token. */
	public CommAction acquireLock() {
		this.lock.lock();
		return new CommAction(this);
	}

	/** Tries to acquire the lock without blocking; returns empty if locked by another thread. */
	public Optional<CommAction> acquireLockTry() {
		if (this.lock.tryLock()) {
			return Optional.of(new CommAction(this));
		}
		return Optional.empty();
	}

	/** Returns true if the underlying port is still open. */
	public boolean isOpen() {
		return this.port.isOpen();
	}

	/**
	 * Serializes {@code object} to JSON and writes it to the port.
	 * <p>
	 * Requires the lock, see {@link #acquireLock()}.
	 *
	 * @param object The command object to serialize and send.
	 */
	public void write(Object object) throws SerialModuleLockRequiredException {
		this.assertLockAcquired();

		byte[] buff;
		try {
			buff = this.getObjectMapper().writeValueAsBytes(object);
		} catch(JsonProcessingException e) {
			throw new RuntimeException("Somehow failed to write json of command.", e);
		}
		log.debug("Writing json to serial port: {}", new String(buff));
		this.port.writeBytes(buff, buff.length);
	}

	/**
	 * Begins a serial transaction: waits for spacing (if requested), checks port is open, and acquires the lock.
	 *
	 * @param waitForCommSpacing if true, blocks until inter-message spacing elapses
	 * @return a scoped {@link CommAction} that releases the lock on close
	 */
	public CommAction startComm(boolean waitForCommSpacing) throws SerialPortClosedException {
		if (!this.isOpen()) {
			throw new SerialPortClosedException();
		}
		if (waitForCommSpacing) {
			this.waitForCommSpacing();
		}

		return this.acquireLock();
	}

	/** Begins a serial transaction, waiting for inter-message spacing. Shorthand for {@link #startComm(boolean)} with {@code true}. */
	public CommAction startComm() throws SerialPortClosedException {
		return this.startComm(true);
	}

	/**
	 * Reads a complete JSON object from the port by tracking brace depth.
	 * <p>
	 * Requires the lock, see {@link #acquireLock()}.
	 *
	 * @return the parsed JSON node.
	 */
	public ObjectNode readJson() throws SerialModuleLockRequiredException, JsonProcessingException {
		this.assertLockAcquired();

		//TODO:: smarter way to accomplish? Scanner?
		StringBuilder sb = new StringBuilder();
		byte[] buffer = new byte[1];
		log.info("Trying to read a json document from serial port...");

		boolean run = true;
		int bracketDepth = 0;
		//read until we get a closing bracket
		do {
			int read = this.port.readBytes(buffer, 1);
			//			log.debug("Read {} bytes ({})", read, (char)buffer[0]);
			if (read > 0) {
				char curchar = (char) buffer[0];
				sb.append(curchar);

				//TODO:: ignore curly brackets in string literals
				if (curchar == '{') {
					bracketDepth++;
				} else if (curchar == '}') {
					bracketDepth--;
					if (bracketDepth == 0) {
						run = false;
					}
				}
			} else {
				run = false;
			}

		} while (run);

		String output = sb.toString().trim();
		log.debug("Got json: {}", output);
		return (ObjectNode) this.getObjectMapper().readTree(output);
	}

	/** Returns true if any bytes are waiting on the port. Requires the lock. */
	public boolean bytesAvailable() throws SerialModuleLockRequiredException {
		this.assertLockAcquired();
		return this.port.bytesAvailable() > 0;
	}

	/** Returns true if enough bytes are available for a minimal JSON message (>= 7 bytes). Requires the lock. */
	public boolean messageAvailable() throws SerialModuleLockRequiredException {
		this.assertLockAcquired();
		int bytesAvailable = this.getPort().bytesAvailable();
		log.debug("Bytes available: {}", bytesAvailable);
		return bytesAvailable >= 7;//7 being the smallest size of a populated json document
	}

	/** Drains all available JSON messages into the queue. Requires the lock. */
	public void readAllMessages(Queue<ObjectNode> queue) throws SerialModuleLockRequiredException, JsonProcessingException {
		this.assertLockAcquired();
		while (this.bytesAvailable()) {
			if (this.messageAvailable()) {
				queue.add(this.readJson());
			}
		}
	}

	/** Blocks until a JSON message arrives or the command-response timeout elapses. Requires the lock. */
	public ObjectNode waitForMessage() throws JsonProcessingException, MssCommandTimeoutException {
		this.assertLockAcquired();
		ZonedDateTime timeoutTime = ZonedDateTime.now().plus(this.getCommandResponseTimeout());
		while (!this.messageAvailable()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				log.error("Interrupted while waiting for message", e);
				throw new RuntimeException("Interrupted while waiting for message.", e);
			}
			//TODO:: timeout
			if(ZonedDateTime.now().isAfter(timeoutTime)) {
				log.error("Timed out waiting for message.");
				throw new MssCommandTimeoutException();
			}
		}
		return this.readJson();
	}

	/** Closes the underlying serial port under the lock. */
	@Override
	public void close() {
		try (
			CommAction r = this.acquireLock()
		) {
			while (!this.port.closePort()) {
				log.warn("Could not close serial port.");
			}
		}
	}
}
