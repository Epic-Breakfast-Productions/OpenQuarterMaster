package com.ebp.openQuarterMaster.driverServer.serial;

import com.ebp.openQuarterMaster.lib.driver.interaction.command.Commands;
import com.fazecast.jSerialComm.SerialPort;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Semaphore;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SerialPortWrapper implements Closeable {
	private static final int TIMEOUT = 500;
	
	@Getter
	private SerialPort port;
	
	private final Semaphore serialSemaphore = new Semaphore(1);
	private boolean hasLock = false;
	
	public SerialPortWrapper(SerialPort port){
		this.port = port;
		this.port.setComPortTimeouts(
			SerialPort.TIMEOUT_READ_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING,
			TIMEOUT,
			TIMEOUT
		);
		
	}
	
	public SerialPortWrapper(String portLocation, Integer baudRate){
		this(SerialPort.getCommPort(portLocation));
		
		if(baudRate != null){
			this.port.setBaudRate(baudRate);
		}
		this.port.openPort();
	}
	
	public SerialPortWrapper(String portLocation){
		this(portLocation, null);
	}
	
	public void acquireLock() throws InterruptedException {
		this.serialSemaphore.acquire();
		this.hasLock = true;
	}
	public void assertHasLock(){
		if(!this.hasLock){
			throw new IllegalStateException("Lock not acquired to talk to serial port "+this.port.getDescriptivePortName()+".");
		}
	}
	public void releaseLock(){
		this.serialSemaphore.release();
		this.hasLock = false;
	}
	
	public String readLine(){
		assertHasLock();
		//TODO:: smarter way to accomplish?
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
	
	public void writeLine(String line){
		assertHasLock();
		
		byte[] buff = (line + Commands.Parts.COMMAND_SEPARATOR_CHAR).getBytes(StandardCharsets.UTF_8);
		this.port.writeBytes(buff, buff.length);
	}
	
	public String readLatestResponse(){
		String latestResponse = null;
		String curLine = null;
		while(!"".equals(curLine) || latestResponse == null){
			curLine = this.readLine();
			if(!curLine.isEmpty()) {
				if (curLine.charAt(0) == Commands.Parts.RETURN_START_CHAR) {
					latestResponse = curLine;
				}
			}
		}
		return latestResponse;
	}
	
	public String sendCommandWithReturn(String command){
		this.writeLine(command);
		return this.readLatestResponse();
	}
	
	public void sendCommandWithoutReturn(String command){
		this.writeLine(command);
		String response = this.readLatestResponse();
		
		if(!"$O".equals(response)){
			throw new IllegalStateException("Not OK from command: " + response);
		}
	}
	
	@Override
	public void close() {
		this.port.closePort();
	}
}
