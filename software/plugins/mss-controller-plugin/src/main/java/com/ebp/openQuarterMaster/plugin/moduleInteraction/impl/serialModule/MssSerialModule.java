package com.ebp.openQuarterMaster.plugin.moduleInteraction.impl.serialModule;

import com.ebp.openQuarterMaster.plugin.moduleInteraction.MssModule;
import com.ebp.openQuarterMaster.plugin.moduleInteraction.command.MssCommand;
import com.ebp.openQuarterMaster.plugin.moduleInteraction.updates.MssUpdate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

/**
 *
 */
public class MssSerialModule extends MssModule {
	
	@Getter(AccessLevel.PRIVATE)
	private SerialPortWrapper serialPortWrapper;
	
	public MssSerialModule(
		ObjectMapper objectMapper,
		String serialPortLocation,
		Optional<Integer> baudRate
	) {
		super(objectMapper);
		this.serialPortWrapper = new SerialPortWrapper(objectMapper, serialPortLocation, baudRate);
		
		this.postConstructInit();
	}
	
	@Override
	public Queue<MssUpdate> getUpdates() {
		//TODO
		return new LinkedList<>();
	}
	
	@Override
	protected ObjectNode sendCommand(MssCommand command) {
		try {
			this.getSerialPortWrapper().acquireLock();
			try{
				this.getSerialPortWrapper().write(command);
				
				while(!this.getSerialPortWrapper().messageAvailable()) {
					Thread.sleep(50);
				}
				
				try {
					return this.getSerialPortWrapper().readJson();
				} catch(JsonProcessingException e) {
					throw new RuntimeException(e);
				}
			} finally {
				this.getSerialPortWrapper().releaseLock();
			}
		} catch(InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	
}