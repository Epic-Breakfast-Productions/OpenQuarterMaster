package tech.ebp.oqm.plugin.mssController.service.mssConn.serial;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.mssController.model.exception.SerialModuleLockRequiredException;
import tech.ebp.oqm.plugin.mssController.model.exception.SerialPortClosedException;
import tech.ebp.oqm.plugin.mssController.model.exception.SerialPortSetupFailedException;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

@Getter(AccessLevel.PRIVATE)
@Slf4j
public class SerialPortWrapper implements AutoCloseable {

	private final ReentrantLock lock = new ReentrantLock();
	private final ObjectMapper objectMapper;
	private final Duration commSpacing;
	private final SerialPort port;
	private ZonedDateTime lastComm = null;


	public SerialPortWrapper(
		ObjectMapper objectMapper,
		String portPath,
		Optional<Integer> baudRate,
		Duration commSpacing,
		Duration readTimeout,
		Duration writeTimeout
	) throws SerialPortSetupFailedException {
		this.objectMapper = objectMapper;
		this.commSpacing = commSpacing;

		SerialPort newPort = SerialPort.getCommPort(portPath);

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
			throw new SerialPortSetupFailedException();
		}

		newPort.setComPortTimeouts(
			SerialPort.TIMEOUT_READ_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING,
			(int) readTimeout.toMillis(),
			(int) writeTimeout.toMillis()
		);

		this.port = newPort;
	}

	private void updateLastComm() {
		this.lastComm = ZonedDateTime.now();
	}

	public void waitForCommSpacing() {
		if (this.lastComm == null) {
			return;
		}
		ZonedDateTime now = ZonedDateTime.now();
		ZonedDateTime timeEnd = this.lastComm.plus(this.getCommSpacing());

		if (now.isBefore(timeEnd)) {
			try {
				Thread.sleep(
					Duration.between(
						now,
						timeEnd
					).toMillis());
			} catch(InterruptedException e) {
				throw new RuntimeException("Failed to wait for comm spacing.", e);
			}
		}
	}


	private void assertLockAcquired() throws SerialModuleLockRequiredException {
		if (!this.getLock().isHeldByCurrentThread()) {
			throw new SerialModuleLockRequiredException();
		}
	}

	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class CommAction implements AutoCloseable {

		private final SerialPortWrapper wrapper;

		@Override
		public void close() {
			this.wrapper.getLock().unlock();
			this.wrapper.updateLastComm();
		}
	}

	public CommAction acquireLock() {
		this.lock.lock();
		return new CommAction(this);
	}

	public Optional<CommAction> acquireLockTry() {
		if (this.lock.tryLock()) {
			return Optional.of(new CommAction(this));
		}
		return Optional.empty();
	}

	public boolean isOpen() {
		return this.port.isOpen();
	}

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

	public CommAction startComm(boolean waitForCommSpacing) throws SerialPortClosedException {
		if (!this.isOpen()) {
			throw new SerialPortClosedException();
		}
		if (waitForCommSpacing) {
			this.waitForCommSpacing();
		}

		return this.acquireLock();
	}

	public CommAction startComm() throws SerialPortClosedException {
		return this.startComm(true);
	}

	/**
	 * Reads a single line from the serial port.
	 * <p>
	 * Requires locked status, see {@link #acquireLock()}.
	 *
	 * @return The line from the serial port.
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

	public boolean bytesAvailable() throws SerialModuleLockRequiredException {
		this.assertLockAcquired();
		return this.port.bytesAvailable() > 0;
	}

	public boolean messageAvailable() throws SerialModuleLockRequiredException {
		this.assertLockAcquired();
		int bytesAvailable = this.getPort().bytesAvailable();
		log.debug("Bytes available: {}", bytesAvailable);
		return bytesAvailable >= 7;//7 being the smallest size of a populated json document
	}

	public void readAllMessages(Queue<ObjectNode> queue) throws SerialModuleLockRequiredException, JsonProcessingException {
		this.assertLockAcquired();
		while (this.bytesAvailable()) {
			if (this.messageAvailable()) {
				queue.add(this.readJson());
			}
		}
	}

	public ObjectNode waitForMessage() throws JsonProcessingException {
		this.assertLockAcquired();
		while (!this.messageAvailable()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				log.error("Interrupted while waiting for message", e);
				throw new RuntimeException("Interrupted while waiting for message.", e);
			}
			//TODO:: timeout
		}
		return this.readJson();
	}

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
