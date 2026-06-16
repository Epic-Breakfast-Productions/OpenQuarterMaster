package tech.ebp.oqm.plugin.mssController.model.moduleComm.state;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlockWeightReport {
	private String weightStr;
	private double weightValue;
	private String weightUnit;
}
