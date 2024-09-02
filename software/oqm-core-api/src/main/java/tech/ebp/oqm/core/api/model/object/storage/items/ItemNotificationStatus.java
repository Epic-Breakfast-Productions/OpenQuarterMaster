package tech.ebp.oqm.core.api.model.object.storage.items;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemNotificationStatus {

	@lombok.Builder.Default
	private boolean lowStock = false;
}
