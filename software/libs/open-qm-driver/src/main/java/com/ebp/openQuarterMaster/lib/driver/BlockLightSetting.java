package com.ebp.openQuarterMaster.lib.driver;

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
	@Min(0)
	private Integer blockNum;
	@lombok.Builder.Default
	private boolean on = false;
	@lombok.Builder.Default
	@NotNull
	@NonNull
	private Color color = Color.WHITE;
	@Min(0)
	@Max(255)
	@lombok.Builder.Default
	private int brightness = 0xFF / 2;
}
