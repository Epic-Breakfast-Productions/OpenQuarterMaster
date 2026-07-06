package tech.ebp.oqm.plugin.mssController.testResources.modules.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.Command;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.GetModuleInfoCommand;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.GetModuleStateCommand;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.highlight.HighlightBlockSetting;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.highlight.HighlightBlocksCommand;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.response.CommandResponse;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.response.CommandResponseType;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.moduleInfo.Capabilities;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.moduleInfo.ModuleInfo;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.state.ModuleState;
import tech.ebp.oqm.plugin.mssController.testResources.modules.TestBlockState;
import tech.ebp.oqm.plugin.mssController.testResources.modules.TestModuleInterface;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static tech.ebp.oqm.plugin.mssController.model.utils.JacksonUtils.OBJECT_MAPPER;

@Slf4j
public class TestModuleEngine {

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
	private ZonedDateTime resetLightsAt = null;

	public TestModuleEngine(ModuleInfo moduleInfo) {
		this.moduleInfo = moduleInfo;

		this.blocks = new ArrayList<>(getModuleInfo().getNumBlocks()) {{
			for (int i = 1; i <= getModuleInfo().getNumBlocks(); i++) {
				this.add(
					TestBlockState.builder()
						.blockNum(i)
						.lightSettings(getModuleInfo().getCapabilities().isBlockLights() ? TestBlockState.TestLightSettings.builder().build() : null)
						.weight(getModuleInfo().getCapabilities().isBlockWeightReporting() ? TestBlockState.TestWeight.builder().build() : null)
						.build()
				);
			}
		}};
	}

	@Builder
	public TestModuleEngine(
		int numBlocks,
		Capabilities capabilities
	) {
		this(
			new ModuleInfo(
				"1.0.0",
				"1.0.0",
				UUID.randomUUID().toString(),
				LocalDate.now(),
				numBlocks,
				capabilities
			)
		);
	}

	public ModuleState getModuleState() {
		return ModuleState.builder()
				   .storageBlocks(
					   this.blocks.stream()
						   .map(TestBlockState::toBlockState)
						   .toList()
				   )
				   .build();
	}

	protected void resetLights() {
		this.blocks.forEach(TestBlockState::resetLights);
	}

	protected TestBlockState getBlock(int blockNum) {
		return this.blocks.stream().filter(b->b.getBlockNum() == blockNum).findFirst().orElseThrow();
	}


	protected CommandResponse handleHighlightBlocksCommand(HighlightBlocksCommand cmd) {
		log.info("Received HighlightBlocksCommand. Handling.");

		if (!cmd.isCarry()) {
			this.resetLights();
		}

		for (HighlightBlockSetting s : cmd.getStorageBlocks()) {
			TestBlockState ts;
			try {
				ts = this.getBlock(s.getStorageBlock());
			} catch(Throwable e) {
				log.error("Error getting block: ", e);
				return CommandResponse.builder()
						   .status(CommandResponseType.R_ERROR)
						   .build();
			}

			ts.getLightSettings().setPowerState(s.getLightPowerState().stateEquivalent);
			ts.getLightSettings().setColor(s.getLightColor());
			ts.getLightSettings().setBrightness(s.getBrightness());
			ts.getLightSettings().setTurnedOnAt(ZonedDateTime.now());
		}

		if (cmd.getDuration() != 0) {
			this.resetLightsAt = ZonedDateTime.now().plus(Duration.of(cmd.getDuration(), ChronoUnit.SECONDS));
		}

		return CommandResponse.builder()
				   .status(CommandResponseType.OK)
				   .build();
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
										 this.getModuleState()
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
				case HighlightBlocksCommand c -> this.handleHighlightBlocksCommand(c);
				default -> throw new IllegalStateException("Unexpected value: " + command);
			};
		} catch(Throwable e) {
			log.error("Error handling command: ", e);
			throw new RuntimeException("Failed to handle command.", e);
		}
	}

	public String handleData(String data) {
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

	public void runTimedTasks() {
		if (this.resetLightsAt != null && this.resetLightsAt.isBefore(ZonedDateTime.now())) {
			log.info("Resetting lights.");
			this.resetLights();
			this.resetLightsAt = null;
		}
	}

}
