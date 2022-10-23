package tech.ebp.oqm.lib.core.units;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import tech.ebp.oqm.lib.core.object.MainObject;

import javax.measure.Unit;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CustomUnitEntry extends MainObject {
	
	@NotNull
	@NonNull
	private UnitCategory category;
	
	@NotNull
	@NonNull
	private Unit<?> unit;
}
