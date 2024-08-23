package tech.ebp.oqm.plugin.mssController.devTools.deployment.testModules;

import tech.ebp.oqm.plugin.mssController.lib.command.response.ModuleInfo;

public class MssTestSerialModule extends MssTestModule<MssTestSerialModule> {
	public MssTestSerialModule(ModuleInfo moduleInfo) {
		super(moduleInfo);
	}


	@Override
	protected void configure() {
		addEnv("moduleConfig.type", "SERIAL");

		//TODO:: directory mapping for serial ports

		super.configure();
	}
}
