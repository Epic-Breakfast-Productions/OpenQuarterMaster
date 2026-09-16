package com.ebp.openQuarterMaster.driverServer.services;

import com.ebp.openQuarterMaster.driverServer.serial.SerialPortWrapper;
import com.ebp.openQuarterMaster.lib.driver.ModuleState;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.Commands;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Singleton;
import java.awt.*;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

/**
 * <a href="https://fazecast.github.io/jSerialComm/">https://fazecast.github.io/jSerialComm/</a>
 */
@Slf4j
@Singleton
public class SerialService {
	
	private static final String GET_STATUS_MESSAGE = "$S\n";
	private static final char RETURN_START_CHAR = '^';
	
	private final Semaphore serialSemaphore = new Semaphore(1);
	private SerialPortWrapper serialPort;
	
	SerialService(
		@ConfigProperty(name = "serial.port")
			String serialPort,
		@ConfigProperty(name = "serial.baud")
			Integer serialBaud
	) {
		this.serialPort = new SerialPortWrapper(serialPort, serialBaud);
	}
	
	public Void setMessage(String message) throws InterruptedException {
		try {
			this.serialPort.acquireLock();
			this.serialPort.sendCommandWithoutReturn(Commands.Parts.COMMAND_START_CHAR + "M|" + message.strip());
		} finally {
			this.serialPort.releaseLock();
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
		try {
			this.serialPort.acquireLock();
			
			String response = this.serialPort.sendCommandWithReturn(GET_STATUS_MESSAGE);
			
			ModuleState.Builder builder = ModuleState.builder();
			
			String[] parts = response.split("\\" + Commands.Parts.SEPARATOR_CHAR);
			
			log.info("Response: {}", response);
			log.debug("Parts: {}", (Object) parts);
			
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
			this.serialPort.releaseLock();
		}
	}
	
	public Uni<ModuleState> getStateUni() throws InterruptedException {
		return Uni.createFrom().item(Unchecked.supplier(this::getState));
	}
}
