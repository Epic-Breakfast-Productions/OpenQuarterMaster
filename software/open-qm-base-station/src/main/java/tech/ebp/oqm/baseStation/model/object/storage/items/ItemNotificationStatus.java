package tech.ebp.oqm.baseStation.model.object.storage.items;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemNotificationStatus {
	
	private boolean lowStock = false;
}
