package tech.ebp.oqm.lib.core.object.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationSettings {
	
	private boolean login = true;
	
	private boolean expiry = true;
	private boolean expiryWarning = true;
}
