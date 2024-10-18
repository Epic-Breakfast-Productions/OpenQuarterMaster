package tech.ebp.oqm.core.api.model.object.interactingEntity.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationSettings {

	@lombok.Builder.Default
	private boolean login = true;

	@lombok.Builder.Default
	private boolean expiry = true;

	@lombok.Builder.Default
	private boolean expiryWarning = true;
}
