package tech.ebp.oqm.core.api.model.object.upgrade;

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
public class TotalUpgradeResult {
	
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
}
