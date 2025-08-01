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
public class OqmDbUpgradeResult implements WasUpgraded {

	private String dbName;
	private Duration timeTaken;
	private List<CollectionUpgradeResult> collectionUpgradeResults;
	
	@Override
	public boolean wasUpgraded() {
		return this.getCollectionUpgradeResults()
				   .stream()
				   .anyMatch(CollectionUpgradeResult::wasUpgraded);
	}
}
