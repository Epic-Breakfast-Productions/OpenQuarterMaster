package tech.ebp.oqm.core.api.model.object.upgrade;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import java.time.Duration;

@Data
@Setter(AccessLevel.PRIVATE)
@Builder
public class CollectionUpgradeResult {

	private String collectionName;
	private long numObjectsUpgraded;
	private long numObjectsNotUpgraded;
	private long numObjectsDeleted;
	private Duration timeTaken;
	@lombok.Builder.Default
	private UpgradeOverallCreatedObjectsResults createdObjects = new UpgradeOverallCreatedObjectsResults();
}
