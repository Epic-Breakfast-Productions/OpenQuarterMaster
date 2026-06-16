package tech.ebp.oqm.plugin.mssController.model.moduleComm.moduleInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Capabilities {

	@Builder.Default
	private boolean blockLights = false;
	@Builder.Default
	private boolean blockLightColor = false;
	@Builder.Default
	private boolean blockLightBrightness = false;
	@Builder.Default
	private boolean blockWeightReporting = false;
}
