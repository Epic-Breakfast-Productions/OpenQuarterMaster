package tech.ebp.oqm.baseStation.model.units;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import tech.ebp.oqm.baseStation.model.object.MainObject;
import tech.ebp.oqm.baseStation.model.rest.unit.custom.NewCustomUnitRequest;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CustomUnitEntry extends MainObject {
	
	@NotNull
	@NonNull
	private UnitCategory category;
	
	@Min(0)
	private long order;
	
	@NotNull
	@NonNull
	private NewCustomUnitRequest unitCreator;
}
