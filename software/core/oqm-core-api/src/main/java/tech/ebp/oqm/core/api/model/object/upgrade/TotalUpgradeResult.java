package tech.ebp.oqm.core.api.model.object.upgrade;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.Setter;

import java.time.Duration;
import java.util.List;

@Data
@Setter(AccessLevel.PRIVATE)
@Builder
public class TotalUpgradeResult implements WasUpgraded {
	
	@NonNull
	private String id;
	@NonNull
	private String instanceId;
	@NonNull
	private Duration timeTaken;
	@NonNull
	private List<CollectionUpgradeResult> topLevelUpgradeResults;
	@NonNull
	private List<OqmDbUpgradeResult> dbUpgradeResults;
	
	@Override
	public boolean wasUpgraded() {
		return this.getTopLevelUpgradeResults()
				   .stream()
				   .anyMatch(CollectionUpgradeResult::wasUpgraded) ||
			   this.getDbUpgradeResults()
				   .stream().anyMatch(OqmDbUpgradeResult::wasUpgraded);
	}
}
