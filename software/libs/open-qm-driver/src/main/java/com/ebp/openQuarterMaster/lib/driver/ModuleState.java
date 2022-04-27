package com.ebp.openQuarterMaster.lib.driver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class ModuleState {
	
	private boolean online = true;
	
	@lombok.Builder.Default
	private List<@NotNull BlockLightSetting> lightSettings = new ArrayList<>();
	private String currentMessage;
	
	//for demo
	private int encoderVal;
	private boolean encoderPressed;
	@lombok.Builder.Default
	private List<String> pixelColors = new ArrayList<>();
}
