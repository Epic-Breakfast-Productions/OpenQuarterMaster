package tech.ebp.oqm.plugin.mssController.model.moduleComm.message.report;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class AmountItemReport extends ItemReport {

	@Min(0)
	private double amount;
	private String unit;
}
