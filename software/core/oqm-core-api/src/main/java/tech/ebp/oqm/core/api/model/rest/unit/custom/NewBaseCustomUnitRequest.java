package tech.ebp.oqm.core.api.model.rest.unit.custom;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import tech.ebp.oqm.core.api.model.units.ValidUnitDimension;
import tech.units.indriya.unit.BaseUnit;

import javax.measure.Unit;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "NewBaseCustomUnitRequest", description = "A request for a new base unit.")
public class NewBaseCustomUnitRequest extends NewCustomUnitRequest {
	
	@NonNull
	@NotNull
	private ValidUnitDimension dimension;
	
	//TODO:: update to standard "type" naming
	@Override
	@Schema(constValue = "BASE", readOnly = true, required = true, examples = "BASE")
	public RequestType getRequestType() {
		return RequestType.BASE;
	}
	
	@Override
	public Unit<?> toUnit() {
		return new BaseUnit<>(this.getSymbol(), this.getName(), this.getDimension().dimension);
	}
}
