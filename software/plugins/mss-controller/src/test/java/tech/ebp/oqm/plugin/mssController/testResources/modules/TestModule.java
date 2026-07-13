package tech.ebp.oqm.plugin.mssController.testResources.modules;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.moduleInfo.ModuleInfo;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.state.ModuleState;
import tech.ebp.oqm.plugin.mssController.service.media.ModuleStateImageService;
import tech.ebp.oqm.plugin.mssController.testResources.modules.engine.TestModuleEngine;

/**
 * Test harness that simulates a physical storage module.
 * Delegates command processing to a {@link TestModuleEngine} and
 * I/O transport to a {@link TestModuleInterface}, exposing the
 * resulting {@link ModuleInfo} and {@link ModuleState} for assertions.
 *
 * <p>Downstream custom types:
 * <ul>
 *   <li>{@code TestModuleEngine} – In-memory command router that parses JSON commands,
 *       mutates block state, and schedules timed light resets.</li>
 *   <li>{@code TestModuleInterface} – Abstract transport layer wrapping a
 *       Jackson {@link com.fasterxml.jackson.databind.ObjectMapper}; subclasses
 *       define the actual send/receive channel (e.g. serial, network).</li>
 *   <li>{@link ModuleInfo} – Immutable snapshot of module metadata: spec/firmware
 *       versions, serial ID, manufacture date, block count, and capabilities.</li>
 *   <li>{@link ModuleState} – Current runtime state holding a list of
 *       {@link tech.ebp.oqm.plugin.mssController.model.moduleComm.state.BlockState}
 *       entries (light settings, weight data, etc.).</li>
 *   <li>{@link ModuleStateImageService} – Static utility that renders an SVG image
 *       from a pair of (ModuleInfo, ModuleState) for visual test verification.</li>
 * </ul>
 */
@Slf4j
public class TestModule implements AutoCloseable {

	@Getter
	private final TestModuleEngine engine;
	private final TestModuleInterface modInterface;


	public TestModule(
		TestModuleEngine engine,
		TestModuleInterface modInterface
	) throws Exception {
		this.engine = engine;
		this.modInterface = modInterface;

		this.modInterface.init();
	}


	@Override
	public void close() throws Exception {
		this.engine.close();
		this.modInterface.close();
	}

	public ModuleInfo getModuleInfo() {
		return this.getEngine().getModuleInfo();
	}

	public ModuleState getModuleState() {
		return this.getEngine().getModuleState();
	}

	public void resetModuleState(){
		this.engine.resetModuleState();
	}

	/** Generate an SVG snapshot of the current module state for visual verification. */
	public String generateStateImage() {
		return ModuleStateImageService.generateStateImage(
			this.getModuleInfo(),
			this.getModuleState()
		);
	}

}
