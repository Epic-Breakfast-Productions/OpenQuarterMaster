package tech.ebp.oqm.plugin.mssController.model.moduleComm.state.light;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockLightSetting {
	@NonNull
	@NotNull
	private BlockLightPowerState powerState;

	private String color;

	@Min(0)
	@Max(255)
	private Integer brightness;
}
