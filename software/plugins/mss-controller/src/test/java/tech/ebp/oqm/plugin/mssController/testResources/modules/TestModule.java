package tech.ebp.oqm.plugin.mssController.testResources.modules;

import lombok.AllArgsConstructor;
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
import java.util.UUID;

@AllArgsConstructor
public class TestModule {

	private final ModuleInfo moduleInfo;
	private final List<BlockState> blocks;
	private final TestModuleInterface testModuleInterface;

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

		this.blocks = new ArrayList<>(){{
			for(int i = 1; i <= numBlocks; i++){
				this.add(
					BlockState.builder()
						.blockNum(i)
						.build()
				);
			}
		}};
	}

	protected CommandResponse handleCommand(Command commandResponse){
		//TODO:: actually process

		return CommandResponse.builder()
			.status(CommandResponseType.OK)
			.build();
	}

}
