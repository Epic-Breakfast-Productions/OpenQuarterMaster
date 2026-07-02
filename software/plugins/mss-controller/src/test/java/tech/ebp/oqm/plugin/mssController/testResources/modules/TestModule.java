package tech.ebp.oqm.plugin.mssController.testResources.modules;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.model.ErrorResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.Command;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.GetModuleInfoCommand;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.GetModuleStateCommand;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.response.CommandResponse;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.response.CommandResponseType;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.moduleInfo.Capabilities;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.moduleInfo.ModuleInfo;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.state.BlockState;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.state.ModuleState;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static tech.ebp.oqm.plugin.mssController.model.utils.JacksonUtils.OBJECT_MAPPER;

@Slf4j
@AllArgsConstructor
public class TestModule implements AutoCloseable {

	private static final long SLEEP_TIME = 100;

	private static final String errFormatResponse;

	static {
		try {
			errFormatResponse = OBJECT_MAPPER.writeValueAsString(CommandResponse.builder().status(CommandResponseType.R_ERROR.ERROR).build());
		} catch(JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}


	@Getter
	private final ModuleInfo moduleInfo;
	private final List<TestBlockState> blocks;
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
					TestBlockState.builder()
						.blockNum(i)
						.lightSettings(capabilities.isBlockLights() ? TestBlockState.TestLightSettings.builder().build() : null)
						.weight(capabilities.isBlockWeightReporting() ? TestBlockState.TestWeight.builder().build() : null)
						.build()
				);
			}
		}};

		this.scheduler.scheduleAtFixedRate(this::iterate, 0, SLEEP_TIME, TimeUnit.MILLISECONDS);
	}

	protected CommandResponse handleModuleInfoCommand(GetModuleInfoCommand data) {
		log.info("Received GetModuleInfoCommand. Handling.");

		CommandResponse output = CommandResponse.builder()
									 .status(CommandResponseType.OK)
									 .response(OBJECT_MAPPER.valueToTree(this.getModuleInfo()))
									 .build();

		log.info("Returning GetModuleInfoCommand response: {}", output);
		return output;
	}

	protected CommandResponse handleGetModuleStateCommand(GetModuleStateCommand data) {
		log.info("Received GetModuleStateCommand. Handling.");

		CommandResponse output = CommandResponse.builder()
									 .status(CommandResponseType.OK)
									 .response(OBJECT_MAPPER.valueToTree(
										 ModuleState.builder()
											 .storageBlocks(
												 this.blocks.stream()
													 .map(TestBlockState::toBlockState)
													 .toList()
											 )
											 .build()
									 ))
									 .build();

		log.info("Returning GetModuleStateCommand response: {}", output);
		return output;
	}

	protected CommandResponse handleCommand(Command command) {
		log.info("Received command {}", command);
		try {
			return switch (command) {
				case GetModuleInfoCommand c -> this.handleModuleInfoCommand(c);
				case GetModuleStateCommand c -> this.handleGetModuleStateCommand(c);
				default -> throw new IllegalStateException("Unexpected value: " + command);
			};
		} catch(Throwable e) {
			log.error("Error handling command: ", e);
			throw new RuntimeException("Failed to handle command.", e);
		}
	}

	protected String handleData(String data) {
		Command command;
		try {
			command = OBJECT_MAPPER.readValue(data, Command.class);
		} catch(JsonProcessingException e) {
			log.error("Error parsing command", e);
			return errFormatResponse;
		}

		try {
			return OBJECT_MAPPER.writeValueAsString(this.handleCommand(command));
		} catch(JsonProcessingException e) {
			log.error("Error writing response from handling data.", e);
			throw new RuntimeException("Failed to write response to string.", e);
		}
	}

	protected void iterate() {
		log.debug("Running TestModuleThread for module {}", this.getModuleInfo().getSerialId());

		Optional<String> received = this.testModuleInterface.receive();

		if (received.isPresent()) {
			log.info("Received data to test module {}: {}", this.getModuleInfo().getSerialId(), received.get());
			String response = this.handleData(received.get());
			log.info("Received response from handleData: {}", response);
			this.testModuleInterface.send(response);
		}
	}

	@Override
	public void close() throws Exception {
		this.scheduler.shutdown();
		this.testModuleInterface.close();
	}


}
