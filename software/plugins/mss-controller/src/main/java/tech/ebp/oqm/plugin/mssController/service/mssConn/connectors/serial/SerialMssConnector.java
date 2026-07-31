package tech.ebp.oqm.plugin.mssController.service.mssConn.connectors.serial;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.vertx.mutiny.core.eventbus.EventBus;
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
import tech.ebp.oqm.plugin.mssController.model.moduleComm.message.Message;
import tech.ebp.oqm.plugin.mssController.service.mssConn.connectors.MssConnector;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;
import java.util.Queue;
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
		EventBus eventBus,
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

		super(validator, mapper, eventBus, moduleConfig);
	}

	@Override
	public Queue<Message> getIncomingMessages() {
		return this.getPort().getReceivedMessages();
	}

	@Override
	protected CommandResponse sendCommandImpl(Command command) throws SerialPortClosedException, JsonProcessingException, MssCommandTimeoutException {
		try (
			SerialPortWrapper.CommAction commAction = this.port.startComm()
		) {
			//TODO:: something to account for incoming messages?

			this.port.write(command);

			Instant timeoutTime = Instant.now().plus(this.getTimings().commandResponseTimeout());
			do{
				Optional<CommandResponse> responseOp = this.getPort().getCommandresponse();

				if(responseOp.isPresent()){
					return responseOp.get();
				}

				try {
					Thread.sleep(100);
				} catch(InterruptedException e) {
					log.error("Interrupted while waiting for message", e);
					throw new RuntimeException("Interrupted while waiting for message.", e);
				}
			}while(timeoutTime.isAfter(Instant.now()));

			throw new MssCommandTimeoutException();
		}
	}

	@Override
	public void close() {
		if(this.port != null){
			this.port.close();
		}
	}

	public Path getPortPath(){
		return this.getPort().getPortPath();
	}
}
