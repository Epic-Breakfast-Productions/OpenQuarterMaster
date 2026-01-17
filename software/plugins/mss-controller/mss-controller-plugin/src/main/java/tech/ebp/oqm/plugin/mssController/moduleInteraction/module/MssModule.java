package tech.ebp.oqm.plugin.mssController.moduleInteraction.module;


import tech.ebp.oqm.plugin.mssController.model.module.OqmModuleInfo;
import tech.ebp.oqm.plugin.mssController.lib.command.GetModInfoCommand;
import tech.ebp.oqm.plugin.mssController.lib.command.HighlightBlocksCommand;
import tech.ebp.oqm.plugin.mssController.lib.command.IdentifyModCommand;
import tech.ebp.oqm.plugin.mssController.lib.command.MssCommand;
import tech.ebp.oqm.plugin.mssController.lib.command.response.CommandResponse;
import tech.ebp.oqm.plugin.mssController.lib.command.response.GetModuleInfoResponse;
import tech.ebp.oqm.plugin.mssController.moduleInteraction.updates.MssUpdate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Queue;

public abstract class MssModule {
	
	public abstract String getInterfaceType();
	
	@Getter(AccessLevel.PROTECTED)
	private final ObjectMapper objectMapper;
	@Getter
	@Setter(AccessLevel.PROTECTED)
	protected OqmModuleInfo moduleInfo;
	
	protected MssModule(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	/**
	 * This should be called at the end of a constructor.
	 */
	protected void postConstructInit(){
		this.sendGetInfoCommand();
	}

	public String getModuleSerialId(){
		return this.getModuleInfo().getModuleSerialId();
	}


	public abstract Queue<MssUpdate> getUpdates();
	
	protected abstract ObjectNode sendCommand(MssCommand command);
	
	protected <T extends CommandResponse> T sendCommand(MssCommand command, Class<T> clazz){
		ObjectNode node = this.sendCommand(command);
		
		T response = null;
		try {
			response = this.getObjectMapper().treeToValue(node, clazz);
		} catch(JsonProcessingException e) {
			throw new RuntimeException(e);//TODO:: handle appropriately
		}
		return response;
	}
	
	public GetModuleInfoResponse sendGetInfoCommand(){
		GetModuleInfoResponse response = this.sendCommand(GetModInfoCommand.getInstance(), GetModuleInfoResponse.class);
		this.getModuleInfo().setModuleInfo(response.getResponse());
		return response;
	}
	
	/**
	 * Clears all highlighted lights on the module by sending a highlight command
	 * with an empty block list and carry=false.
	 */
	public void clearAllLights() {
		HighlightBlocksCommand clearCommand = new HighlightBlocksCommand();
		clearCommand.setCarry(false);
		clearCommand.setBeep(false);
		clearCommand.setDuration(0);
		this.sendCommand(clearCommand, CommandResponse.class);
	}

	public CommandResponse sendModuleIdentifyCommand(){
		clearAllLights();
		return this.sendCommand(IdentifyModCommand.getInstance(), CommandResponse.class);
	}

	public CommandResponse sendModuleBlockIdentifyCommand(int blockNum){
		clearAllLights();
		HighlightBlocksCommand command = new HighlightBlocksCommand();
		command.getStorageBlocks().add(new HighlightBlocksCommand.BlockHighlightSettings(blockNum));

		return this.sendCommand(command, CommandResponse.class);
	}
	
	public CommandResponse sendBlockHighlightCommand(
		HighlightBlocksCommand command
	){
		//TODO:: check for valid block numbers
		return this.sendCommand(command, CommandResponse.class);
	}
}
