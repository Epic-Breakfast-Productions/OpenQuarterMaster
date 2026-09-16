package tech.ebp.oqm.core.api.service.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.notification.processing.ItemProcessResults;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessResultsWrapper {
	@NonNull
	private ObjectId oqmDbId;
	@NonNull
	private ItemProcessResults processResults;
}
