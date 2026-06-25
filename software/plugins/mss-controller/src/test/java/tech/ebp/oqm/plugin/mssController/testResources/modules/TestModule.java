package tech.ebp.oqm.plugin.mssController.testResources.modules;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.model.ErrorResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.Command;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.response.CommandResponse;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.response.CommandResponseType;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.moduleInfo.Capabilities;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.moduleInfo.ModuleInfo;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.state.BlockState;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@AllArgsConstructor
public class TestModule implements AutoCloseable {
	private static final long SLEEP_TIME = 100;
	private final static ObjectMapper objectMapper = new ObjectMapper();

	private static final String errFormatResponse;

	static {
		try {
			errFormatResponse = objectMapper.writeValueAsString(CommandResponse.builder().status(CommandResponseType.ERROR).build());
		} catch(JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}


	@Getter
	private final ModuleInfo moduleInfo;
	private final List<BlockState> blocks;
	private final TestModuleInterface testModuleInterface;

	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();



	public TestModule(
		int numBlocks,
		Capabilities capabilities,
		TestModuleInterface testModuleInterface
	) {
		this.moduleInfo = new ModuleInfo(
			"1.0.0",
			"1.0.0",
			UUID.randomUUID().toString(),
			LocalDate.now(),
			numBlocks,
			capabilities
		);
		this.testModuleInterface = testModuleInterface;

		this.blocks = new ArrayList<>() {{
			for (int i = 1; i <= numBlocks; i++) {
				this.add(
					BlockState.builder()
						.blockNum(i)
						.build()
				);
			}
		}};

		this.scheduler.scheduleAtFixedRate(this::iterate, 0, SLEEP_TIME, TimeUnit.MILLISECONDS);
	}

	protected CommandResponse handleCommand(Command commandResponse) {
		//TODO:: actually process

		return CommandResponse.builder()
				   .status(CommandResponseType.OK)
				   .build();
	}

	protected String handleData(String data) {
		Command command;
		try{
			command = objectMapper.readValue(data, Command.class);
		} catch (JsonProcessingException e) {
			log.error("Error parsing command", e);
			return errFormatResponse;
		}

		try {
			return objectMapper.writeValueAsString(this.handleCommand(command));
		} catch(JsonProcessingException e) {
			throw new RuntimeException("Failed to write response to string.", e);
		}
	}

	protected void iterate(){
		log.debug("Running TestModuleThread for module {}", this.getModuleInfo().getSerialId());

		Optional<String> received = this.testModuleInterface.receive();

		if(received.isPresent()){
			log.info("Received data to test module {}: {}", this.getModuleInfo().getSerialId(), received.get());
			this.handleData(received.get());
		}
	}

	@Override
	public void close() throws Exception {
		this.scheduler.shutdown();
		this.testModuleInterface.close();
	}

}
