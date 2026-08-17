package tech.ebp.oqm.plugin.mssController.service.mssConn.connectors;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.mssController.model.exception.command.MssCommandError;
import tech.ebp.oqm.plugin.mssController.model.exception.command.MssCommandReturnedError;
import tech.ebp.oqm.plugin.mssController.model.exception.module.ModuleSetupFailedException;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.MssConnectorInfo;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.Command;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.GetModuleInfoCommand;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.GetModuleStateCommand;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.response.CommandResponse;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.response.CommandResponseType;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.message.Message;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.moduleInfo.ModuleInfo;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.state.ModuleState;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public abstract class MssConnector {

	@NonNull
	@NotNull
	@Getter(AccessLevel.PROTECTED)
	private final EventBus eventBus;

	@NonNull
	@NotNull
	@Getter
	private final ModuleInfo moduleInfo;

	@NonNull
	@NotNull
	@Getter
	private ModuleState lastModuleState;

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

	public abstract Queue<Message> getIncomingMessages();

	public Optional<Message> getNextMessage(){
		try{
			return Optional.of(this.getIncomingMessages().remove());
		} catch(NoSuchElementException e) {
			return Optional.empty();
		}
	}


	protected void resetErrsSinceLastComm(){
		this.errsSinceLastComm = new ArrayList<>();
	}

	protected void setLastComm(ZonedDateTime lastComm){
		this.lastComm = lastComm;
		this.resetErrsSinceLastComm();
	}

	protected MssConnector(
		Validator validator,
		ObjectMapper objectMapper,
		EventBus eventBus,
		Object moduleConfig
	) throws ModuleSetupFailedException {
		log.info("Initializing new MSS module connector.");
		this.eventBus = eventBus;

		CommandResponse response = null;
		try {
			response = this.sendCommand(GetModuleInfoCommand.builder().build());
		} catch(Exception e) {
			this.setConnState(ConnState.FAIL);
			throw new ModuleSetupFailedException(moduleConfig, "Failed to get module info during init.", e);
		}
		log.info("Received response from module during init: {}.", response);

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

		try {
			response = this.sendCommand(new GetModuleStateCommand());
		} catch(Exception e) {
			this.setConnState(ConnState.FAIL);
			throw new ModuleSetupFailedException(moduleConfig, "Failed to get module state during init.", e);
		}
		log.info("Received response from module during init: {}.", response);

		try {
			this.lastModuleState = objectMapper.treeToValue(response.getResponse(), ModuleState.class);
		} catch(JsonProcessingException e) {
			throw new ModuleSetupFailedException(moduleConfig, "Failed to get module state during init. Failed to parse state.", e);
		}

		log.info("Module initialized. Module info: {}", this.moduleInfo);
	}

	protected abstract CommandResponse sendCommandImpl(Command command) throws Exception;

	public String getSerialId(){
		return this.getModuleInfo().getSerialId();
	}

	public CommandResponse sendCommand(@Valid Command command) throws MssCommandError {
		log.info("Sending command: {}", command);
		CommandResponse response = null;
		try {
			response = this.sendCommandImpl(command);
		} catch(Exception e) {
			throw new MssCommandError("Failed to run command.", e);
		}
		log.info("Command response: {}", response);

		if(!CommandResponseType.OK.equals(response.getStatus())){
			log.warn("Command returned with error: {} / {}", response.getStatus(), response);
			throw new MssCommandReturnedError(
				this.getSerialId(),
				command,
				response
			);
		}

		this.setLastComm(ZonedDateTime.now());
		return response;
	}

	public MssConnectorInfo toConnInfo(){
		return MssConnectorInfo.builder()
				   .specVersion(this.getModuleInfo().getSpecVersion())
				   .firmwareVersion(this.getModuleInfo().getFirmwareVersion())
				   .serialId(this.getSerialId())
				   .manufactureDate(this.getModuleInfo().getManufactureDate())
				   .numBlocks(this.getModuleInfo().getNumBlocks())
				   .capabilities(this.getModuleInfo().getCapabilities())
				   .lastComm(this.getLastComm())
				   .numErrsSinceLastComm(this.getErrsSinceLastComm().size())
				   .connState(this.getConnState())
				   .build();
	}
}
