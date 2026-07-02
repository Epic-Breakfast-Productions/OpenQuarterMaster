package tech.ebp.oqm.plugin.mssController.model.moduleComm.state;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlockWeightReport {
	private String weightStr;
	private double weightValue;
	private String weightUnit;
}
