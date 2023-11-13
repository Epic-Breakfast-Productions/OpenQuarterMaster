package tech.ebp.oqm.baseStation.model.rest.unit.custom;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.baseStation.model.units.ValidUnitDimension;
import tech.units.indriya.unit.BaseUnit;

import javax.measure.Unit;

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
