package tech.ebp.oqm.plugin.mssController.service.mssConn;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.mssController.model.exception.ModuleSetupFailedException;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.Command;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.GetModuleInfoCommand;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.response.CommandResponse;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.response.CommandResponseType;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.moduleInfo.ModuleInfo;

import java.time.ZonedDateTime;

@Slf4j
public abstract class MssConnector {

	@NonNull
	@NotNull
	@Getter
	private final ModuleInfo moduleInfo;

	@NonNull
	@NotNull
	@Getter
	@Setter(AccessLevel.PROTECTED)
	private ZonedDateTime lastComm;

	protected MssConnector(ObjectMapper objectMapper) throws ModuleSetupFailedException {
		CommandResponse response = this.sendCommand(GetModuleInfoCommand.builder().build());

		if(!CommandResponseType.OK.equals(response.getStatus())){
			log.error("Could not get module info: {}", response);
			throw new ModuleSetupFailedException("Could not get module info.");
		}

		ObjectNode responseData = response.getResponse();

		try {
			this.moduleInfo = objectMapper.treeToValue(responseData, ModuleInfo.class);
		} catch(JsonProcessingException e) {
			throw new ModuleSetupFailedException("Failed to parse module info from command response.", e);
		}
	}

	protected abstract CommandResponse sendCommandImpl(Command command);

	public CommandResponse sendCommand(@Valid Command command){
		log.info("Sending command: {}", command);
		CommandResponse response = this.sendCommandImpl(command);
		log.info("Command response: {}", response);
		//TODO:: error check
		this.setLastComm(ZonedDateTime.now());
		return response;
	}
}
