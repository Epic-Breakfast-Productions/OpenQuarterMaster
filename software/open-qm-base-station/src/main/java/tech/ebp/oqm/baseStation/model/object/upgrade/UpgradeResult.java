package tech.ebp.oqm.baseStation.model.object.upgrade;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import tech.ebp.oqm.baseStation.model.object.Versionable;

import jakarta.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@Builder
public class UpgradeResult<T extends Versionable> {
	
	@NotNull
	@NonNull
	private T upgradedObject;
	
	private int oldVersion;
	
	private boolean wasUpgraded(){
		return this.getOldVersion() < upgradedObject.getObjectVersion();
	}
}
