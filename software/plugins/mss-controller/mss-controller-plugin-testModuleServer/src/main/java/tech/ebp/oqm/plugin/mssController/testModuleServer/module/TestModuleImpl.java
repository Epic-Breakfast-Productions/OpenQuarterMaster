package tech.ebp.oqm.plugin.mssController.testModuleServer.module;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Getter;
import tech.ebp.oqm.plugin.mssController.lib.command.MssCommand;
import tech.ebp.oqm.plugin.mssController.lib.command.response.CommandResponse;
import tech.ebp.oqm.plugin.mssController.lib.command.response.CommandResponseStatus;
import tech.ebp.oqm.plugin.mssController.lib.command.response.ModuleInfo;
import tech.ebp.oqm.plugin.mssController.testModuleServer.config.ModuleConfig;

@ApplicationScoped
public class TestModuleImpl {

	@Getter
	private ModuleInfo moduleInfo;

	@Inject
	public TestModuleImpl(ModuleConfig moduleConfig) {
		this.moduleInfo = new ModuleInfo(
			moduleConfig.specVersion(),
			moduleConfig.serialId(),
			moduleConfig.manufactureDate(),
			moduleConfig.numBlocks()
		);
	}

	public CommandResponse process(MssCommand command) {
		//TODO:: this
		return new CommandResponse(CommandResponseStatus.OK, "");
	}
}
