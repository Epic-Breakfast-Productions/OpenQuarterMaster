package tech.ebp.oqm.core.api.model.object.upgrade;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.Setter;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.Versionable;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Data
@Setter(AccessLevel.PRIVATE)
@Builder
public class ObjectUpgradeResult<T extends Versionable> implements WasUpgraded {
	
	@NonNull
	@NotNull
	private ObjectId objectId;
	
	/**
	 * The object that was upgraded.
	 */
	@NotNull
	@NonNull
	private Optional<T> upgradedObject;
	
	/**
	 * The time it took to upgrade
	 */
	@NotNull
	@NonNull
	private Duration timeTaken;
	
	/**
	 * The previous version of the object.
	 */
	private int oldVersion;
	
	/**
	 * Objects that were created as a result of upgrading this object.
	 */
	@NotNull
	@NonNull
	private UpgradeCreatedObjectsResults upgradeCreatedObjects;
	
	/**
	 * If we are to just delete the object.
	 */
	@lombok.Builder.Default
	private boolean delObj = false;
	
	/**
	 * If the object was actually upgraded.
	 *
	 * @return If the object was actually upgraded.
	 */
	public boolean wasUpgraded() {
		if(this.getUpgradedObject().isEmpty()){
			return false;
		}
		return this.getOldVersion() < this.getUpgradedObject().get().getSchemaVersion();
	}
	
	/**
	 * Gets the number of versions that were bumped to get to the latest.
	 *
	 * @return the number of versions that were bumped to get to the latest.
	 */
	public int getNumVersionsBumped() {
		if(this.getUpgradedObject().isEmpty()){
			return 0;
		}
		return this.getUpgradedObject().get().getSchemaVersion() - this.getOldVersion();
	}
	
	/**
	 * Determines if there were created objects as a result of upgrading.
	 *
	 * @return if there were created objects as a result of upgrading.
	 */
	public boolean hasUpgradedCreatedObjects() {
		return !this.getUpgradeCreatedObjects().isEmpty() &&
			   !this.getUpgradeCreatedObjects().values().stream().allMatch(List::isEmpty);
	}
}