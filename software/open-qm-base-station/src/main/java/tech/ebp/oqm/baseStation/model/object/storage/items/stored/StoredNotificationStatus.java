package tech.ebp.oqm.baseStation.model.object.storage.items.stored;

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
