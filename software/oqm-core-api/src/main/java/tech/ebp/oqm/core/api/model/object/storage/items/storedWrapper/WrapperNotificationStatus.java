package tech.ebp.oqm.core.api.model.object.storage.items.storedWrapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WrapperNotificationStatus {
	
	private boolean lowStock = false;
}
