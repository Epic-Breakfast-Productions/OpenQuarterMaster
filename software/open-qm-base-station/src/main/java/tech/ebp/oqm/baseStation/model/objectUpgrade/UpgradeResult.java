package tech.ebp.oqm.baseStation.model.objectUpgrade;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import tech.ebp.oqm.baseStation.model.object.Versionable;

import javax.validation.constraints.NotNull;
import java.time.Duration;

@Data
@AllArgsConstructor
@Builder
public class UpgradeResult<T extends Versionable> {
	
	@NotNull
	@NonNull
	private T upgradedObject;
	
	@NotNull
	@NonNull
	private Duration timeToUpgrade;
	
	private int oldVersion;
	
	public boolean wasUpgraded(){
		return this.getOldVersion() < upgradedObject.getSchemaVersion();
	}
}
