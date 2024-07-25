package tech.ebp.oqm.plugin.mssController.moduleInteraction.module.serialModule;

import tech.ebp.oqm.plugin.mssController.moduleInteraction.module.MssModule;
import tech.ebp.oqm.plugin.mssController.devTools.runtime.model.command.HighlightBlocksCommand;
import tech.ebp.oqm.plugin.mssController.devTools.runtime.model.command.MssCommand;
import tech.ebp.oqm.plugin.mssController.devTools.runtime.model.command.response.CommandResponse;
import tech.ebp.oqm.plugin.mssController.moduleInteraction.updates.MssUpdate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;
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
	public String getInterfaceType() {
		return "serialOverUSB";
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
			try {
				this.getSerialPortWrapper().write(command);
				
				while (!this.getSerialPortWrapper().messageAvailable()) {
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
	
	@Override
	public CommandResponse sendBlockHighlightCommand(HighlightBlocksCommand command) {
		CommandResponse lastCommand = null;
		boolean first = true;
		for (HighlightBlocksCommand.BlockHighlightSettings curSetting : command.getStorageBlocks()) {
			lastCommand = this.sendCommand(
				new HighlightBlocksCommand(
					command.getDuration(),
					command.isCarry() || !first,
					false,
					List.of(curSetting)
				),
				CommandResponse.class
			);
			
			first = false;
		}
		
		if (command.isBeep()) {
			lastCommand = this.sendCommand(
				new HighlightBlocksCommand(
					command.getDuration(),
					true,
					true,
					List.of()
				),
				CommandResponse.class
			);
		}
		
		return lastCommand;
	}
}
