package tech.ebp.oqm.plugin.mssController.model.moduleComm.message.report;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.state.BlockWeightState;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class BlockWeightReport extends BlockWeightState {

	@NotNull
	@Min(1)
	private int blockNum;

}
