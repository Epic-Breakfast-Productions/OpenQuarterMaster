package tech.ebp.oqm.lib.moduleDriver;

import tech.ebp.oqm.lib.moduleDriver.interaction.command.Commands;
import tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.CommandParsingUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.awt.*;

/**
 * This describes a storage block's light settings.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlockLightSetting {
	
	/**
	 * The power state of the light.
	 */
	@NotNull
	@NonNull
	@lombok.Builder.Default
	private BlockLightPowerState powerState = BlockLightPowerState.OFF;
	
	/**
	 * The color of the light.
	 */
	@NotNull
	@NonNull
	@lombok.Builder.Default
	private Color color = Color.WHITE;
	
	/**
	 * The brightness of the light (higher value, the brighter).
	 */
	@Min(0)
	@Max(255)
	@lombok.Builder.Default
	private int brightness = 127;// 0xFF / 2
	
	/**
	 * Applies a specific setting string (from the specification).
	 *
	 * @param setting The setting string to use.
	 *
	 * @return This setting object.
	 */
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
