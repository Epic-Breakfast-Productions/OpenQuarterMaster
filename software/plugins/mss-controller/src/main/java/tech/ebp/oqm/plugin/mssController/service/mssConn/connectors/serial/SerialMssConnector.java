package tech.ebp.oqm.plugin.mssController.service.mssConn.connectors.serial;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.Validator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.mssController.config.ModuleConfig;
import tech.ebp.oqm.plugin.mssController.model.exception.ModuleSetupFailedException;
import tech.ebp.oqm.plugin.mssController.model.exception.MssCommandTimeoutException;
import tech.ebp.oqm.plugin.mssController.model.exception.SerialPortClosedException;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.Command;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.response.CommandResponse;
import tech.ebp.oqm.plugin.mssController.service.mssConn.connectors.MssConnector;

import java.util.concurrent.locks.ReentrantLock;


@Getter(AccessLevel.PRIVATE)
@Slf4j
public class SerialMssConnector extends MssConnector implements AutoCloseable {

	private final ObjectMapper objectMapper;
	private final ReentrantLock lock = new ReentrantLock();
	private final ModuleConfig.SerialConfig.SerialModuleConfig moduleConfig;
	private final ModuleConfig.SerialConfig.Timings timings;


	/**
	 * The actual serial port to interact with.
	 */
	private SerialPortWrapper port;


	public SerialMssConnector(
		Validator validator,
		ObjectMapper mapper,
		ModuleConfig.SerialConfig.SerialModuleConfig moduleConfig,
		ModuleConfig.SerialConfig.Timings timings
	) throws ModuleSetupFailedException {
		this.objectMapper = mapper;
		this.moduleConfig = moduleConfig;
		this.timings = timings;
		this.port = new SerialPortWrapper(
			mapper,
			moduleConfig.portPath(),
			moduleConfig.baudRate(),
			timings.commSpacing(),
			timings.rwTimeout(),
			timings.rwTimeout(),
			timings.commandResponseTimeout()
		);

		super(validator, mapper, moduleConfig);
	}

	@Override
	protected CommandResponse sendCommandImpl(Command command) throws SerialPortClosedException, JsonProcessingException, MssCommandTimeoutException {
		try (
			SerialPortWrapper.CommAction r = this.port.startComm()
		) {
			this.port.readAllMessages(this.getIncomingMessages());

			this.port.write(command);

			ObjectNode responseJson = this.port.waitForMessage();
			return this.getObjectMapper().treeToValue(responseJson, CommandResponse.class);
		}
	}

	@Override
	public void close() {
		if(this.port != null){
			this.port.close();
		}
	}
}
