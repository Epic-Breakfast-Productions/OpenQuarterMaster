package com.ebp.openQuarterMaster.services;

import com.fazecast.jSerialComm.SerialPort;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Semaphore;

@Slf4j
@Singleton
public class SerialService {
	
	private static final byte[] GET_STATUS_MESSAGE = "$S\n".getBytes(StandardCharsets.UTF_8);
	private static final char RETURN_START_CHAR = '^';
	
	private final Semaphore serialSemaphore = new Semaphore(1);
	private final String serialPortStr;
	private final int serialPortBaud;
	private SerialPort serialPort;
	
	SerialService(
		@ConfigProperty(name = "serial.port")
			String serialPort,
		@ConfigProperty(name = "serial.baud")
			Integer serialBaud
	) {
		this.serialPortStr = serialPort;
		this.serialPortBaud = serialBaud;
		
		this.initSerialPort();
	}
	
	private void initSerialPort() {
		this.serialPort = SerialPort.getCommPort(this.serialPortStr);
		this.serialPort.setBaudRate(this.serialPortBaud);
		this.serialPort.openPort();
	}
	
	
	private void assertSerialOpen() {
		if (!this.serialPort.isOpen()) {
			throw new IllegalStateException("Serial port was not open.");
		}
	}
	
	private String readLine() {
		StringBuilder sb = new StringBuilder();
		byte[] buffer = new byte[1];
		log.info("Trying to read a line from serial port...");
		
		do {
			if (this.serialPort.readBytes(buffer, 1) > 0) {
				if (buffer[0] == '\n') {
					break;
				}
				sb.append((char) buffer[0]);
			}
		} while (true);
		
		log.info("Got line: {}", sb.toString());
		return sb.toString();
	}
	
	
	public void setMessage(String message) throws InterruptedException {
		assertSerialOpen();
		try {
			this.serialSemaphore.acquire();
			
			//TODO:: make this a command instead of just writing the message
			byte[] messageBytes = (message + '\n').getBytes(StandardCharsets.UTF_8);
			this.serialPort.writeBytes(messageBytes, messageBytes.length);
			
		} finally {
			this.serialSemaphore.release();
		}
	}
	
	public Uni<Void> setMessageUni(String message) throws InterruptedException {
		return Uni.createFrom().item(()->{return this.setMessage(message);});
	}
	
	public State getState() throws InterruptedException {
		assertSerialOpen();
		try {
			this.serialSemaphore.acquire();
			
			this.serialPort.writeBytes(GET_STATUS_MESSAGE, GET_STATUS_MESSAGE.length);
			
			String response = null;
			
			while (response == null) {
				String cur = readLine();
				if (
					!cur.isBlank() &&
					cur.charAt(0) == RETURN_START_CHAR
				) {
					response = cur;
				}
			}
			
			StateBuilder builder;
			
			
			return builder.build();
		} finally {
			this.serialSemaphore.release();
		}
		
	}
	
	public Uni<State> getStateUni() throws InterruptedException {
		return Uni.createFrom().item(()->this.getState());
	}
}
