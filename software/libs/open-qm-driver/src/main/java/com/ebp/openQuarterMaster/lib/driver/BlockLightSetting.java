package com.ebp.openQuarterMaster.lib.driver;

import com.ebp.openQuarterMaster.lib.driver.interaction.command.Commands;
import com.ebp.openQuarterMaster.lib.driver.interaction.command.commands.CommandParsingUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.awt.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlockLightSetting {
	
	@NotNull
	@NonNull
	@lombok.Builder.Default
	private BlockLightPowerState powerState = BlockLightPowerState.OFF;
	
	@NotNull
	@NonNull
	@lombok.Builder.Default
	private Color color = Color.WHITE;
	
	@Min(0)
	@Max(255)
	@lombok.Builder.Default
	private int brightness = 127;// 0xFF / 2
	
	@lombok.Builder.Default
	private boolean flashing = false;
	
	public BlockLightSetting applySetting(String setting) {
		Object settingObj = CommandParsingUtils.getLightSettingAsObj(setting);
		
		if (settingObj instanceof BlockLightPowerState) {
			this.powerState = (BlockLightPowerState) settingObj;
		} else if (settingObj instanceof Color) {
			this.color = (Color) settingObj;
		} else if (settingObj instanceof Integer) {
			this.brightness = (int) settingObj;
		} else {
			throw new IllegalStateException("Should not get this; unhandled object type from light setting.");
		}
		
		return this;
	}
	
	public BlockLightSetting applySettings(String... settings) {
		for (String curSetting : settings) {
			this.applySetting(curSetting);
		}
		return this;
	}
	
	public BlockLightSetting applySettings(String settings) {
		return this.applySettings(
			settings.split("" + Commands.Parts.COMPOSITE_SEPARATOR_CHAR)
		);
	}
}
