package tech.ebp.oqm.core.api.model.object.upgrade;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import java.time.Duration;
import java.util.List;

@Data
@Setter(AccessLevel.PRIVATE)
@Builder
public class OqmDbUpgradeResult {

	private String dbName;
	private Duration timeTaken;
	private List<CollectionUpgradeResult> collectionUpgradeResults;
}
