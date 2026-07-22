package tech.ebp.oqm.plugin.mssController.model.moduleComm.state;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.state.light.BlockLightSetting;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlockState {

	@NotNull
	@Min(1)
	private int blockNum;

	private BlockLightSetting lightSettings;
	private BlockWeightState weightState;
	private LockState lockState;
}
