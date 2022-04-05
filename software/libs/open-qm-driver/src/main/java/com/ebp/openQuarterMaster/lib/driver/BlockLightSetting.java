package com.ebp.openQuarterMaster.lib.driver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
	private boolean on;
	private Color color;
	@Min(0)
	private byte brightness;
}
