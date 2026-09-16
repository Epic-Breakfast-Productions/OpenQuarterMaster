package tech.ebp.oqm.plugin.mssController.devTools.deployment.testModules;


import tech.ebp.oqm.plugin.mssController.lib.command.response.ModuleInfo;

public class MssTestNetworkModule extends MssTestModule {

	private String username;
	private String password;

	protected MssTestNetworkModule(ModuleInfo moduleInfo) {
		super(moduleInfo);
	}
}
