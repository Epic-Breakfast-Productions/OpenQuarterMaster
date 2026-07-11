package tech.ebp.oqm.plugin.mssController.testResources.modules;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.state.BlockState;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.state.BlockWeightState;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.state.light.BlockLightPowerState;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.state.light.BlockLightSetting;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@Builder
public class TestBlockState {
	private final int blockNum;
	private final TestLightSettings lightSettings;
	private final TestWeight weight;

	public BlockState toBlockState(){
		return BlockState.builder()
				   .blockNum(blockNum)
				   .lightSettings(this.lightSettings == null ? null : this.lightSettings.toBlockLightSetting())
				   .weightState(this.weight == null ? null : this.weight.toWeightState())
				   .build();
	}


	@Data
	@AllArgsConstructor
	@Builder
	public static class TestLightSettings {
		@Builder.Default
		private ZonedDateTime turnedOnAt = null;
		@Builder.Default
		private BlockLightPowerState powerState = BlockLightPowerState.OFF;
		@Builder.Default
		private String color = "#FFFFFF";
		@Builder.Default
		private Integer brightness = 255;


		public BlockLightSetting toBlockLightSetting(){
			return BlockLightSetting.builder()
					   .color(this.color)
					   .powerState(this.powerState)
					   .brightness(this.brightness)
					   .build();
		}

		public void reset(){
			this.turnedOnAt = null;
			this.powerState = BlockLightPowerState.OFF;
		}

	}

	@Data
	@AllArgsConstructor
	@Builder
	public static class TestWeight {
		private double weightValue;
		private String weightUnit;

		public BlockWeightState toWeightState(){
			return BlockWeightState.builder()
					   .weightUnit(this.weightUnit)
					   .weightValue(this.weightValue)
					   .weightStr(this.weightValue + this.weightUnit)
					   .build();
		}
	}

	public void resetLights(){
		this.lightSettings.reset();
	}
}
