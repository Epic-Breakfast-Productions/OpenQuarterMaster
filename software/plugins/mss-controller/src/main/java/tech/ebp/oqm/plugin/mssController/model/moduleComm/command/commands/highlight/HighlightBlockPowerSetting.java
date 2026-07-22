package tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.highlight;

import tech.ebp.oqm.plugin.mssController.model.moduleComm.state.light.BlockLightPowerState;

public enum HighlightBlockPowerSetting {
	ON(BlockLightPowerState.ON),
	FLASHING(BlockLightPowerState.FLASHING)
	;

	public final BlockLightPowerState stateEquivalent;

	HighlightBlockPowerSetting(BlockLightPowerState stateEquivalent) {
		this.stateEquivalent = stateEquivalent;
	}
}
