package tech.ebp.oqm.core.api.model.object.upgrade;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import tech.ebp.oqm.core.api.model.object.Versionable;

import java.time.Duration;

@Data
@Setter(AccessLevel.PRIVATE)
@Builder
public class ObjectUpgradeResult<T extends Versionable> {

	@NotNull
	@NonNull
	private T upgradedObject;

	@NotNull
	@NonNull
	private Duration timeTaken;

	private int oldVersion;
	
	@NotNull
	@NonNull
	private UpgradeCreatedObjectsResults upgradeCreatedObjects;

	public boolean wasUpgraded(){
		return this.getOldVersion() < this.getUpgradedObject().getSchemaVersion();
	}
	
	public int getNumVersionsBumped(){
		return this.getUpgradedObject().getSchemaVersion() - this.getOldVersion();
	}
	
	public boolean hasUpgradedCreatedObjects(){
		//TODO:: consider values?
		return this.getUpgradeCreatedObjects().isEmpty();
	}
}