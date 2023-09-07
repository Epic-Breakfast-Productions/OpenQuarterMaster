package com.ebp.openQuarterMaster.plugin.moduleInteraction.impl.serialModule;

import com.ebp.openQuarterMaster.plugin.moduleInteraction.command.MssCommand;
import com.ebp.openQuarterMaster.plugin.moduleInteraction.impl.serialModule.exceptions.CommandAssertionError;
import com.ebp.openQuarterMaster.plugin.moduleInteraction.impl.serialModule.exceptions.CommandReturnedErrorException;
import com.ebp.openQuarterMaster.plugin.moduleInteraction.impl.serialModule.exceptions.SerialInteractionUnlockedException;
import com.ebp.openQuarterMaster.plugin.moduleInteraction.updates.MssUpdate;
import com.fazecast.jSerialComm.SerialPort;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.Semaphore;

/**
 * Wrapper for a bare {@link SerialPort}, to provide additional utilities and functionalities.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SerialPortWrapper implements Closeable {
	
	private static final int TIMEOUT = 500;//ms
	
	/**
	 * The actual serial port to interact with.
	 */
	@Getter
	private SerialPort port;
	
	private final Semaphore serialSemaphore = new Semaphore(1);
	private boolean hasLock = false;
	
	@Getter
	private final LinkedList<MssUpdate> receivedCommands = new LinkedList<>();
	
	public SerialPortWrapper(SerialPort port) {
		this.port = port;
		this.port.setComPortTimeouts(
			SerialPort.TIMEOUT_READ_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING,
			TIMEOUT,
			TIMEOUT
		);
	}
	
	public SerialPortWrapper(String portLocation, @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<Integer> baudRate) {
		this(SerialPort.getCommPort(portLocation));
		
		baudRate.ifPresent(rate->this.port.setBaudRate(rate));
		
		this.port.openPort();
		
		//TODO:: figure out best place to init? check connection?
	}
	
	public SerialPortWrapper(String portLocation) {
		this(portLocation, null);
	}
	
	/**
	 * Acquires the lock on this object/serial port.
	 * <p>
	 * Required for most interactions with the serial port.
	 * <p>
	 * To release the lock, see {@link #releaseLock()}
	 * <p>
	 * Best used in the following manner:
	 * <pre>
	 *     wrapper.acquireLock();
	 *     try{
	 *         wrapper...
	 *         ...
	 *     } finally {
	 *         wrapper.releaseLock();
	 *     }
	 * </pre>
	 *
	 * @throws InterruptedException
	 */
	public void acquireLock() throws InterruptedException {
		this.serialSemaphore.acquire();
		this.hasLock = true;
	}
	
	/**
	 * Releases the mutex lock held by this object.
	 * <p>
	 * See {@link #acquireLock()} for more details.
	 */
	public void releaseLock() {
		this.hasLock = false;
		this.serialSemaphore.release();
	}
	
	/**
	 * Asserts that the lock is held by this thread.
	 *
	 * @throws SerialInteractionUnlockedException If the lock is not held by this thread.
	 */
	public void assertHasLock() throws SerialInteractionUnlockedException {
		if (!this.hasLock) {
			throw new SerialInteractionUnlockedException(
				"Lock not acquired to talk to serial port " + this.port.getDescriptivePortName() + "."
			);
		}
	}
	
	/**
	 * Reads a single line from the serial port.
	 * <p>
	 * Requires locked status, see {@link #acquireLock()}.
	 *
	 * @return The line from the serial port.
	 * @throws SerialInteractionUnlockedException If the lock on the serial port is not held.
	 */
	public String readLine() throws SerialInteractionUnlockedException {
		assertHasLock();
		//TODO:: smarter way to accomplish? Scanner?
		StringBuilder sb = new StringBuilder();
		byte[] buffer = new byte[1];
		log.info("Trying to read a line from serial port...");
		
		boolean run = true;
		do {
			int read = this.port.readBytes(buffer, 1);
			//			log.debug("Read {} bytes ({})", read, (char)buffer[0]);
			if (read > 0) {
				if (buffer[0] == '\n') {
					log.info("Got Newline.");
					break;
				}
				//ignore carriage returns
				if (buffer[0] == '\r') {
					continue;
				}
				sb.append((char) buffer[0]);
			} else {
				run = false;
			}
		} while (run);
		
		String output = sb.toString();
		log.info("Got line: \"{}\"", output);
		return output;
	}
//
//	/**
//	 * Processes all lines waiting to be read sent from the module.
//	 * <p>
//	 * Requires locked status, see {@link #acquireLock()}.
//	 *
//	 * @return The queue of commands received from the module. Same object as {@link #getReceivedCommands()}
//	 * @throws SerialInteractionUnlockedException If the lock on the serial port is not held.
//	 */
//	public Queue<Command> processLines() throws SerialInteractionUnlockedException {
//		this.assertHasLock();
//
//		String curLine;
//		do {
//			curLine = this.readLine();
//
//			if (Commands.isLog(curLine)) {
//				log.info("LOG FROM MODULE: {}", curLine);
//			}
//			if (Commands.isCommand(curLine)) {
//				this.receivedCommands.add(CommandParser.parse(curLine));
//			}
//		} while (curLine != null);
//
//		return this.receivedCommands;
//	}
//
//	/**
//	 * Calls {@link #processLines()} until at least one command is returned. Returns the last received command from the module.
//	 * <p>
//	 * Requires locked status, see {@link #acquireLock()}.
//	 *
//	 * @return The last received command from the module.
//	 * @throws SerialInteractionUnlockedException If the lock on the serial port is not held.
//	 */
//	public Command readLatestResponse() throws SerialInteractionUnlockedException {
//		Command latestResponse = null;
//		long start = System.currentTimeMillis();
//		while (true) {
//			this.processLines();
//
//			if (!this.receivedCommands.isEmpty()) {
//				latestResponse = this.receivedCommands.removeLast();
//				break;
//			}
//			//timeout if we've been waiting for no response.
//			if (System.currentTimeMillis() - start > TIMEOUT) {
//				break;
//			}
//		}
//
//		if (latestResponse == null) {
//			throw new IllegalStateException("Failed to read command from module.");//TODO:: proper exception
//		}
//		return latestResponse;
//	}
//
//	/**
//	 * Writes a command to the serial port, adding {@link Commands.Parts#COMMAND_SEPARATOR_CHAR} to the end.
//	 * <p>
//	 * Requires locked status, see {@link #acquireLock()}.
//	 *
//	 * @param command The command to write to the serial port.
//	 *
//	 * @throws SerialInteractionUnlockedException If the lock on the serial port is not held.
//	 */
//	public void writeLine(String command) throws SerialInteractionUnlockedException {
//		assertHasLock();
//
//		byte[] buff = (command + Commands.Parts.COMMAND_SEPARATOR_CHAR).getBytes(StandardCharsets.UTF_8);
//		this.port.writeBytes(buff, buff.length);
//	}
//
//	/**
//	 * Writes a command to the serial port.
//	 * <p>
//	 * Requires locked status, see {@link #acquireLock()}.
//	 *
//	 * @param command The command to write to the serial port.
//	 *
//	 * @throws SerialInteractionUnlockedException If the lock on the serial port is not held.
//	 */
//	public void writeCommand(Command command) throws SerialInteractionUnlockedException {
//		this.writeLine(command.serialLine());
//	}
//
//	/**
//	 * Writes a command to the serial port, and waits/reads the response from the module.
//	 * <p>
//	 * Requires locked status, see {@link #acquireLock()}.
//	 *
//	 * @param command The command to write to the serial port.
//	 * @param assertOk Assert that the returned command is {@link OkCommand}
//	 *
//	 * @return The command returned by the module.
//	 * @throws SerialInteractionUnlockedException If the lock on the serial port is not held.
//	 * @throws CommandReturnedErrorException If the command returned by the module was an error.
//	 * @throws CommandAssertionError If asserting returned is OK, and was not OK.
//	 */
//	public MssCommand sendCommand(MssCommand command, boolean assertOk) throws SerialInteractionUnlockedException,
//																			 CommandReturnedErrorException, CommandAssertionError {
//		this.writeCommand(command);
//		MssCommand returned = this.readLatestResponse();
//
//		if (ERROR.equals(returned.getType())) {
//			throw new CommandReturnedErrorException((ErrorCommand) returned, "Command result was in error.");
//		}
//
//		if (assertOk) {
//			if (!OkCommand.getInstance().equals(returned)) {
//				throw new CommandAssertionError(command, "Returned command was not OKAY.");
//			}
//		}
//
//		return returned;
//	}
//
//	/**
//	 * Writes a command to the serial port, and waits/reads the response from the module.
//	 * <p>
//	 * Wrapper for {@link #sendCommand(Command, boolean)}
//	 * <p>
//	 * Requires locked status, see {@link #acquireLock()}.
//	 *
//	 * @param command The command to write to the serial port.
//	 *
//	 * @return The command returned by the module.
//	 * @throws SerialInteractionUnlockedException If the lock on the serial port is not held.
//	 * @throws CommandReturnedErrorException If the command returned by the module was an error.
//	 */
//	public Command sendCommand(Command command) throws SerialInteractionUnlockedException,
//														   CommandReturnedErrorException {
//		return this.sendCommand(command, false);
//	}
	
	/**
	 * Closes the held serial port.
	 */
	@Override
	public void close() {
		this.port.closePort();
	}
}
