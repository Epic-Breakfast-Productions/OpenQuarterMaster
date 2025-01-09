package tech.ebp.oqm.core.api.model.units;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.rest.unit.custom.NewCustomUnitRequest;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CustomUnitEntry extends MainObject {
	public static final int CUR_SCHEMA_VERSION = 1;
	
	@NotNull
	@NonNull
	private UnitCategory category;
	
	@Min(0)
	private long order;
	
	@NotNull
	@NonNull
	private NewCustomUnitRequest unitCreator;

	@Override
	public int getSchemaVersion() {
		return CUR_SCHEMA_VERSION;
	}
}
