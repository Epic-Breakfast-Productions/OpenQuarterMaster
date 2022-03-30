package com.ebp.openQuarterMaster;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fazecast.jSerialComm.SerialPort;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Semaphore;

@Path("/")
@Singleton
@Slf4j
public class SerialManagerInterface {
	
	private static final byte[] GET_STATUS_MESSAGE = "$S\n".getBytes(StandardCharsets.UTF_8);
	private static final char RETURN_START_CHAR = '^';
	
	private final Semaphore serialSemaphore = new Semaphore(1);
	
	private SerialPort serialPort;
	private final ObjectMapper objectMapper;
	
	private String readLine() {
		StringBuilder sb = new StringBuilder();
		byte[] buffer = new byte[1];
		log.info("Trying to read a line from serial port...");
		
		
		do {
			if(this.serialPort.readBytes(buffer, 1) > 0) {
				if (buffer[0] == '\n') {
					break;
				}
				sb.append((char)buffer[0]);
			}
		} while (true);
		
		log.info("Got line: {}", sb.toString());
		return sb.toString();
	}
	
	SerialManagerInterface(
		@ConfigProperty(name = "serial.port")
			String serialPort,
		@ConfigProperty(name = "serial.baud")
			Integer serialBaud,
		ObjectMapper mapper
	) {
		this.serialPort = SerialPort.getCommPort(serialPort);
		this.serialPort.setBaudRate(serialBaud);
		this.serialPort.openPort();
		this.objectMapper = mapper;
	}
	
	private void assertSerialOpen(){
		if(!this.serialPort.isOpen()){
			throw new IllegalStateException("Serial port was not open.");
		}
	}
	
	@GET
	@Path("/getState")
	@Produces(MediaType.TEXT_PLAIN)
	public JsonNode hello() throws InterruptedException, IOException {
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
			
			ObjectNode output = this.objectMapper.createObjectNode();
			
			
			return output;
		} finally {
			this.serialSemaphore.release();
		}
	}
}
