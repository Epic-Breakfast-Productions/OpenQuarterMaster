package tech.ebp.oqm.core.api.model.object.upgrade;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Duration;

@Data
@Setter(AccessLevel.PRIVATE)
@SuperBuilder(toBuilder = true)
public class CollectionUpgradeResult implements WasUpgraded {
	
	@NonNull
	@NotNull
	private Class<?> collectionClass;
	@NonNull
	@NotNull
	private String collectionName;
	private long numObjectsUpgraded;
	private long numObjectsNotUpgraded;
	private long numObjectsDeleted;
	@NonNull
	@NotNull
	private Duration timeTaken;
	
	@lombok.Builder.Default
	private UpgradeOverallCreatedObjectsResults createdObjects = new UpgradeOverallCreatedObjectsResults();
	
	@Override
	public boolean wasUpgraded() {
		return this.getNumObjectsUpgraded() > 0;
	}
}
