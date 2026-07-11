package tech.ebp.oqm.plugin.mssController.testResources.modules;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.moduleInfo.ModuleInfo;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.state.ModuleState;
import tech.ebp.oqm.plugin.mssController.service.media.ModuleStateImageService;
import tech.ebp.oqm.plugin.mssController.testResources.modules.engine.TestModuleEngine;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TestModule implements AutoCloseable {

	private static final long SLEEP_TIME = 100;

	@Getter
	private final TestModuleEngine engine;
	private final TestModuleInterface modInterface;


	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();


	public TestModule(
		TestModuleEngine engine,
		TestModuleInterface modInterface
	) throws Exception {
		this.engine = engine;
		this.modInterface = modInterface;

		this.modInterface.init();

		this.scheduler.scheduleAtFixedRate(this::iterate, 0, SLEEP_TIME, TimeUnit.MILLISECONDS);
	}


	protected void iterate() {
		log.debug("Running TestModuleThread for module {}", this.getModuleInfo().getSerialId());

		this.getEngine().runTimedTasks();
	}

	@Override
	public void close() throws Exception {
		this.scheduler.shutdown();
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

	public String generateStateImage() {
		return ModuleStateImageService.generateStateImage(
			this.getModuleInfo(),
			this.getModuleState()
		);
	}

}
