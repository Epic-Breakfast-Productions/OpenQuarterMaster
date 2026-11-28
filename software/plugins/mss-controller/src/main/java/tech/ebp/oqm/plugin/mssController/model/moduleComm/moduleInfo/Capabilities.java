package tech.ebp.oqm.plugin.mssController.model.moduleComm.moduleInfo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter(AccessLevel.PRIVATE)
public class Capabilities {

	@Builder.Default
	private boolean blockLights = false;
	@Builder.Default
	private boolean blockLightColor = false;
	@Builder.Default
	private boolean blockLightBrightness = false;
	@Builder.Default
	private boolean blockWeights = false;
	@Builder.Default
	private boolean itemEventReporting = false;
	@Builder.Default
	private boolean blockLocking = false;
	@Builder.Default
	private boolean openState = false;
	@Builder.Default
	private boolean userNotifying = false;

}
