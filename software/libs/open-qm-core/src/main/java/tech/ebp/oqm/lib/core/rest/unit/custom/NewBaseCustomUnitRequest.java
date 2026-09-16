package tech.ebp.oqm.lib.core.rest.unit.custom;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.lib.core.units.ValidUnitDimension;
import tech.units.indriya.unit.BaseUnit;
import tech.units.indriya.unit.UnitDimension;

import javax.measure.Unit;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class NewBaseCustomUnitRequest extends NewCustomUnitRequest {
	
	@NonNull
	@NotNull
	private ValidUnitDimension dimension;
	
	@Override
	public RequestType getRequestType() {
		return RequestType.BASE;
	}
	
	@Override
	public Unit<?> toUnit() {
		return new BaseUnit<>(this.getSymbol(), this.getName(), this.getDimension().dimension);
	}
}
