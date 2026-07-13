package tech.ebp.oqm.plugin.mssController.testResources.modules;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.moduleInfo.ModuleInfo;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.state.ModuleState;
import tech.ebp.oqm.plugin.mssController.service.media.ModuleStateImageService;
import tech.ebp.oqm.plugin.mssController.testResources.modules.engine.TestModuleEngine;

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

	public String generateStateImage() {
		return ModuleStateImageService.generateStateImage(
			this.getModuleInfo(),
			this.getModuleState()
		);
	}

}
