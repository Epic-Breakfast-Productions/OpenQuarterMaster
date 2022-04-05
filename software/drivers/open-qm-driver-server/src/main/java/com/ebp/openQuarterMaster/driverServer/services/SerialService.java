package com.ebp.openQuarterMaster.driverServer.services;

import com.ebp.openQuarterMaster.lib.driver.ModuleState;
import com.ebp.openQuarterMaster.lib.driver.interaction.Commands;
import com.fazecast.jSerialComm.SerialPort;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Singleton;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

/**
 * https://fazecast.github.io/jSerialComm/
 */
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
	
	/**
	 * TODO:: do with scanner?
	 * @return
	 */
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
	
	private String getResponse(){
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
		response = response.strip();
		return response;
	}
	
	
	public Void setMessage(String message) throws InterruptedException {
		assertSerialOpen();
		try {
			this.serialSemaphore.acquire();
			
			//TODO:: make this a command instead of just writing the message
			byte[] messageBytes = (message + '\n').getBytes(StandardCharsets.UTF_8);
			this.serialPort.writeBytes(messageBytes, messageBytes.length);
			
		} finally {
			this.serialSemaphore.release();
		}
		return null;
	}
	
	public Uni<Void> setMessageUni(String message) throws InterruptedException {
		return Uni.createFrom()
				  .item(
					  Unchecked.supplier(()->this.setMessage(message)
					  ));
	}
	
	public ModuleState getState() throws InterruptedException {
		assertSerialOpen();
		try {
			this.serialSemaphore.acquire();
			
			this.serialPort.writeBytes(GET_STATUS_MESSAGE, GET_STATUS_MESSAGE.length);
			
			String response = getResponse();
			
			ModuleState.Builder builder = ModuleState.builder();
			
			String[] parts = response.split("\\" + Commands.Parts.SEPARATOR_CHAR);
			
			log.info("Response: {}", response);
			log.debug("Parts: {}", (Object) parts);
			
			builder.serialNo(UUID.randomUUID().toString());
			builder.encoderVal(Integer.parseInt(parts[1]));
			builder.encoderPressed(Boolean.parseBoolean(parts[2]));
			builder.currentMessage(parts[3]);
			builder.pixelColors(
				Arrays.stream(Arrays.copyOfRange(parts, 4, parts.length))
					.map(
						(String colorIntStr)->{
							int colorInt = Integer.parseInt(colorIntStr);
							Color color = new Color(colorInt);
							return "#"+Integer.toHexString(color.getRGB()).substring(2);
						}
					).collect(Collectors.toList())
			);
			
			return builder.build();
		} finally {
			this.serialSemaphore.release();
		}
		
	}
	
	public Uni<ModuleState> getStateUni() throws InterruptedException {
		return Uni.createFrom().item(Unchecked.supplier(this::getState));
	}
}
