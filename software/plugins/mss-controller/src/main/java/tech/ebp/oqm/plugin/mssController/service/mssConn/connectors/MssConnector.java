package tech.ebp.oqm.plugin.mssController.service.mssConn.connectors;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public abstract class MssConnector {

	@NonNull
	@NotNull
	@Getter
	private final ModuleInfo moduleInfo;

	@Getter
	private final Queue<ObjectNode> incomingMessages = new ArrayDeque<>();

	@NonNull
	@NotNull
	@Getter
	private ZonedDateTime lastComm;

	@NonNull
	@NotNull
	@Getter
	@Setter(AccessLevel.PRIVATE)
	private List<Exception> errsSinceLastComm;

	@NonNull
	@NotNull
	@Getter
	@Setter(AccessLevel.PROTECTED)
	private ConnState connState;

	protected void resetErrsSinceLastComm(){
		this.errsSinceLastComm = new ArrayList<>();
	}

	protected void setLastComm(ZonedDateTime lastComm){
		this.lastComm = lastComm;
		this.resetErrsSinceLastComm();
	}

	protected MssConnector(Validator validator, ObjectMapper objectMapper, Object moduleConfig) throws ModuleSetupFailedException {
		log.info("Initializing new MSS module connector.");
		CommandResponse response = null;
		try {
			response = this.sendCommand(GetModuleInfoCommand.builder().build());
		} catch(Exception e) {
			this.setConnState(ConnState.FAIL);
			throw new ModuleSetupFailedException(moduleConfig, "Failed to get module info during init.", e);
		}
		log.info("Received response from module during init: {}.", response);

		if(!CommandResponseType.OK.equals(response.getStatus())){
			log.error("Could not get module info: {}", response);
			this.setConnState(ConnState.FAIL);
			throw new ModuleSetupFailedException(moduleConfig, "Could not get module info.");
		}

		ObjectNode responseData = response.getResponse();

		try {
			this.moduleInfo = objectMapper.treeToValue(responseData, ModuleInfo.class);
		} catch(JsonProcessingException e) {
			this.setConnState(ConnState.FAIL);
			throw new ModuleSetupFailedException(moduleConfig, "Failed to parse module info from command response.", e);
		}

		Set<ConstraintViolation<ModuleInfo>> violations = validator.validate(this.moduleInfo);
		if(!violations.isEmpty()) {
			this.setConnState(ConnState.FAIL);
			throw new ModuleSetupFailedException(
				moduleConfig,
				"Failed to validate module info. Violations: " +
				violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(", "))
				);
		}

		this.setConnState(ConnState.OK);

		log.info("Module initialized. Module info: {}", this.moduleInfo);
	}

	protected abstract CommandResponse sendCommandImpl(Command command) throws Exception;

	public CommandResponse sendCommand(@Valid Command command) throws Exception {
		log.info("Sending command: {}", command);
		CommandResponse response = this.sendCommandImpl(command);
		log.info("Command response: {}", response);
		//TODO:: error check
		this.setLastComm(ZonedDateTime.now());
		return response;
	}
}
