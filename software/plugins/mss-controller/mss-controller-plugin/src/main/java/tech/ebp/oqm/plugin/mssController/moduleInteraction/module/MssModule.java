package tech.ebp.oqm.plugin.mssController.moduleInteraction.module;


import tech.ebp.oqm.plugin.mssController.model.module.OqmModuleInfo;
import tech.ebp.oqm.plugin.mssController.devTools.runtime.model.command.GetModInfoCommand;
import tech.ebp.oqm.plugin.mssController.devTools.runtime.model.command.HighlightBlocksCommand;
import tech.ebp.oqm.plugin.mssController.devTools.runtime.model.command.IdentifyModCommand;
import tech.ebp.oqm.plugin.mssController.devTools.runtime.model.command.MssCommand;
import tech.ebp.oqm.plugin.mssController.devTools.runtime.model.command.response.CommandResponse;
import tech.ebp.oqm.plugin.mssController.devTools.runtime.model.command.response.GetModuleInfoResponse;
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
	
	public CommandResponse sendModuleIdentifyCommand(){
		return this.sendCommand(IdentifyModCommand.getInstance(), CommandResponse.class);
	}
	
	public CommandResponse sendModuleBlockIdentifyCommand(int blockNum){
		//TODO:: make identify command for block
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
