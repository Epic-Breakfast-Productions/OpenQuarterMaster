package tech.ebp.oqm.core.api.model.rest.management;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
public class CollectionClearResult {
	@NonNull
	private String collectionName;
	@NonNull
	private Long numRecordsDeleted;
}
