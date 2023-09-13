package com.ebp.openQuarterMaster.plugin.moduleInteraction;


import com.ebp.openQuarterMaster.plugin.moduleInteraction.command.GetModInfoCommand;
import com.ebp.openQuarterMaster.plugin.moduleInteraction.command.MssCommand;
import com.ebp.openQuarterMaster.plugin.moduleInteraction.command.response.CommandResponse;
import com.ebp.openQuarterMaster.plugin.moduleInteraction.command.response.GetModuleInfoResponse;
import com.ebp.openQuarterMaster.plugin.moduleInteraction.command.response.ModuleInfo;
import com.ebp.openQuarterMaster.plugin.moduleInteraction.updates.MssUpdate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Queue;

public abstract class MssModule {
	
	@Getter(AccessLevel.PROTECTED)
	private final ObjectMapper objectMapper;
	@Getter
	@Setter(AccessLevel.PROTECTED)
	protected ModuleInfo moduleInfo;
	
	protected MssModule(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
	protected MssModule(ObjectMapper objectMapper, ModuleInfo moduleInfo) {
		this(objectMapper);
		this.moduleInfo = moduleInfo;
	}
	
	protected void postConstructInit(){
		this.sendGetInfoCommand();
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
		this.setModuleInfo(response.getResponse());
		return response;
	}
}
