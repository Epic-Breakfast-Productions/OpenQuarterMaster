package tech.ebp.oqm.core.api.model.object.storage.items.stored;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StoredNotificationStatus {
	
	private boolean expired = false;
	private boolean expiredWarning = false;
}
