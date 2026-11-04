package tech.ebp.oqm.plugin.mssController.testResources.modules.engine;

import lombok.Data;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.moduleInfo.ModuleInfo;
import tech.ebp.oqm.plugin.mssController.testResources.modules.TestBlockState;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class TestModuleEngineState {

	private List<TestBlockState> blocks;
	private ZonedDateTime resetLightsAt;
	private boolean reportsPaused = false;

	public TestModuleEngineState(ModuleInfo info) {
		this.blocks = new ArrayList<>(info.getNumBlocks()) {{
			for (int i = 1; i <= info.getNumBlocks(); i++) {
				this.add(
					TestBlockState.builder()
						.blockNum(i)
						.lightSettings(info.getCapabilities().isBlockLights() ? TestBlockState.TestLightSettings.builder().build() : null)
						.weight(info.getCapabilities().isItemEventReporting() ? TestBlockState.TestWeight.builder().build() : null)
						.build()
				);
			}
		}};
	}
}
