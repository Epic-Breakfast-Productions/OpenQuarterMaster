package tech.ebp.oqm.core.api.model.object.upgrade;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import tech.ebp.oqm.core.api.model.object.Versionable;

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
