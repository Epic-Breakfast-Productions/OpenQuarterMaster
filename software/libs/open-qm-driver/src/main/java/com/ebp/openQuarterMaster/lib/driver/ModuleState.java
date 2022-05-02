package com.ebp.openQuarterMaster.lib.driver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * Describes the state of a module.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class ModuleState {
	
	/**
	 * The light settings of the module.
	 * <p>
	 * Ordered list, matching the size and order of what is held by the module.
	 */
	@lombok.Builder.Default
	private List<@NotNull BlockLightSetting> lightSettings = new ArrayList<>();
	
	/**
	 * The message currently displayed by the module.
	 */
	@NotNull
	@NonNull
	@lombok.Builder.Default
	private String currentMessage = "";
	
	//for demo
	
	private int encoderVal;
	private boolean encoderPressed;
	
	@lombok.Builder.Default
	private List<String> pixelColors = new ArrayList<>();
}
