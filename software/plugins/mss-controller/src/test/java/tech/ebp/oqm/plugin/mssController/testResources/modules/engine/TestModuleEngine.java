package tech.ebp.oqm.plugin.mssController.testResources.modules.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.Command;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.CalibrateWeightsCommand;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.ClearHighlightCommand;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.GetModuleInfoCommand;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.GetModuleStateCommand;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.LockBlocksCommand;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.NotifyUserCommand;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.PauseAction;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.PauseReportsCommand;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.highlight.HighlightBlockSetting;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.highlight.HighlightBlocksCommand;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.response.CommandResponse;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.response.CommandResponseType;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.moduleInfo.Capabilities;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.moduleInfo.ModuleInfo;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.state.LockState;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.state.ModuleState;
import tech.ebp.oqm.plugin.mssController.testResources.modules.TestBlockState;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static tech.ebp.oqm.plugin.mssController.model.utils.JacksonUtils.OBJECT_MAPPER;

/**
 * In-memory simulator for a physical storage module.
 * <p>Parses incoming JSON commands ({@link Command} subclasses), mutates
 * block-level state ({@link tech.ebp.oqm.plugin.mssController.testResources.modules.TestBlockState}),
 * and runs a scheduled executor to reset lights after timed durations.
 *</p>
 */
@Slf4j
public class TestModuleEngine implements AutoCloseable {

	private static final long SLEEP_TIME = 100;

	private static final String ERR_FORMAT_RESPONSE;

	static {
		try {
			ERR_FORMAT_RESPONSE = OBJECT_MAPPER.writeValueAsString(CommandResponse.builder().status(CommandResponseType.R_ERROR).build());
		} catch(JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	@Getter
	private final ModuleInfo moduleInfo;
	@Getter
	private TestModuleEngineState state;

	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

	public TestModuleEngine(ModuleInfo moduleInfo) {
		this.moduleInfo = moduleInfo;

		this.resetModuleState();

		this.scheduler.scheduleAtFixedRate(this::iterate, 0, SLEEP_TIME, TimeUnit.MILLISECONDS);
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
				"2026-07-13",
				numBlocks,
				capabilities
			)
		);
	}

	public ModuleState getModuleState() {
		return ModuleState.builder()
				   .storageBlocks(
					   this.getState().getBlocks().stream()
						   .map(TestBlockState::toBlockState)
						   .toList()
				   )
				   .build();
	}

	protected void resetLights() {
		this.getState().getBlocks().forEach(TestBlockState::resetLights);
		this.getState().setResetLightsAt(null);
	}

	protected TestBlockState getBlock(int blockNum) {
		return this.getState().getBlocks().stream().filter(b->b.getBlockNum() == blockNum).findFirst().orElseThrow();
	}


	protected CommandResponse handleHighlightBlocksCommand(HighlightBlocksCommand cmd) {
		log.info("Received HighlightBlocksCommand. Handling.");

		if (!cmd.isCarry()) {
			this.resetLights();
		}

		for (HighlightBlockSetting s : cmd.getStorageBlocks()) {
			TestBlockState ts;
			try {
				ts = this.getBlock(s.getBlockNum());
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
			this.getState().setResetLightsAt(ZonedDateTime.now().plus(Duration.of(cmd.getDuration(), ChronoUnit.SECONDS)));
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

	protected CommandResponse handleClearHighlightCommand(ClearHighlightCommand c) {
		log.info("Received ClearHighlightCommand. Handling.");

		this.resetLights();

		return CommandResponse.builder()
				   .status(CommandResponseType.OK)
				   .build();
	}

	private CommandResponse handleCalibrateWeightsCommand(CalibrateWeightsCommand c) {
		log.info("Received CalibrateWeightsCommand. Handling.");

		List<Integer> blockNums = c.getStorageBlocks();

		if(blockNums == null || blockNums.isEmpty()) {
			blockNums = IntStream.range(1, this.getModuleInfo().getNumBlocks() + 1).boxed().toList();
		}

		for (int blockNum : blockNums) {
			TestBlockState block;
			try {
				block = this.getBlock(blockNum);
			} catch(Throwable e) {
				log.error("Error getting block: ", e);
				return CommandResponse.builder()
						   .status(CommandResponseType.R_ERROR)
						   .build();
			}

			block.getWeight().setWeightValue(0.0);
		}

		return CommandResponse.builder()
				   .status(CommandResponseType.OK)
				   .build();
	}

	private CommandResponse handleNotifyUserCommand(NotifyUserCommand c) {
		log.info("Received NotifyUserCommand with action: {}. Handling.", c.getAction());

		switch (c.getAction()) {
			case INV_UPDATE_FAILED -> log.warn("Inventory update failed notification received.");
			default -> CommandResponse.builder()
						   .status(CommandResponseType.R_ERROR)
						   .build();
		}

		return CommandResponse.builder()
				   .status(CommandResponseType.OK)
				   .build();
	}

	private CommandResponse handleLockBlocksCommand(LockBlocksCommand c) {
		log.info("Received LockBlocksCommand. Handling.");

		for (int blockNum : c.getStorageBlocks()) {
			TestBlockState block;
			try {
				block = this.getBlock(blockNum);
			} catch(Throwable e) {
				log.error("Error getting block: ", e);
				return CommandResponse.builder()
						   .status(CommandResponseType.R_ERROR)
						   .build();
			}

			if (c.getAction() == LockBlocksCommand.LockAction.LOCK) {
				block.setLockState(LockState.LOCKED);
			} else if (c.getAction() == LockBlocksCommand.LockAction.UNLOCK) {
				block.setLockState(LockState.UNLOCKED);
			}
		}

		return CommandResponse.builder()
				   .status(CommandResponseType.OK)
				   .build();
	}

	private CommandResponse handlePauseReportsCommand(PauseReportsCommand c) {
		log.info("Received PauseReportsCommand. Handling.");

		if (c.getAction() == PauseAction.PAUSE) {
			this.getState().setReportsPaused(true);
		} else if (c.getAction() == PauseAction.UNPAUSE) {
			this.getState().setReportsPaused(false);
		}

		return CommandResponse.builder()
			   .status(CommandResponseType.OK)
			   .build();
	}



	protected CommandResponse handleCommand(Command command) {
		log.info("Received command {}", command);
		try {
			return switch (command) {
				case GetModuleInfoCommand c -> this.handleModuleInfoCommand(c);
				case GetModuleStateCommand c -> this.handleGetModuleStateCommand(c);
				case HighlightBlocksCommand c -> this.handleHighlightBlocksCommand(c);
				case ClearHighlightCommand c -> this.handleClearHighlightCommand(c);
				case CalibrateWeightsCommand c -> this.handleCalibrateWeightsCommand(c);
				case PauseReportsCommand c -> this.handlePauseReportsCommand(c);
				case LockBlocksCommand c -> this.handleLockBlocksCommand(c);
				case NotifyUserCommand c -> this.handleNotifyUserCommand(c);
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
			return ERR_FORMAT_RESPONSE;
		}

		try {
			return OBJECT_MAPPER.writeValueAsString(this.handleCommand(command));
		} catch(JsonProcessingException e) {
			log.error("Error writing response from handling data.", e);
			throw new RuntimeException("Failed to write response to string.", e);
		}
	}

	public void runTimedTasks() {
		if (this.getState().getResetLightsAt() != null && this.getState().getResetLightsAt().isBefore(ZonedDateTime.now())) {
			log.info("Resetting lights.");
			this.resetLights();
		}
	}

	public void resetModuleState() {
		log.info("Resetting module state for module: {}", this.getModuleInfo().getSerialId());
		this.state = new TestModuleEngineState(this.getModuleInfo());
	}

	//TODO:: reporting


	protected void iterate() {
		log.debug("Running TestModuleThread for module {}", this.getModuleInfo().getSerialId());

		this.runTimedTasks();
	}


	@Override
	public void close() throws Exception {
		this.scheduler.shutdown();
	}

}
