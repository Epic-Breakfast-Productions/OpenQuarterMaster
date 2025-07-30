package tech.ebp.oqm.core.api.model.rest.management;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
@Builder
public class DbClearResult {
	@NonNull
	private String dbName;
	@NonNull
	private String dbId;
	@NonNull
	private List<CollectionClearResult> collectionClearResults;
}
