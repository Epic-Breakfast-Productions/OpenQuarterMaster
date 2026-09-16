package tech.ebp.oqm.lib.core.rest.unit.custom;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.lib.core.units.UnitTools;
import tech.ebp.oqm.lib.core.validation.annotations.ValidUnit;

import javax.measure.Unit;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class NewDerivedCustomUnitRequest extends NewCustomUnitRequest {
	
	@NotNull
	@NonNull
	@ValidUnit
	private Unit<?> baseUnit;
	
	@NotNull
	@NonNull
	private BigDecimal numPerBaseUnit;
	
	@NotNull
	@NonNull
	private DeriveType deriveType;
	
	@Override
	public RequestType getRequestType() {
		return RequestType.DERIVED;
	}
	
	@Override
	public Unit<?> toUnit() {
		Unit<?> newUnit;
		switch (this.getDeriveType()) {
			case multiply:
				newUnit = this.getBaseUnit().multiply(this.getNumPerBaseUnit());
				break;
			case divide:
				newUnit = this.getBaseUnit().divide(this.getNumPerBaseUnit());
				break;
			default:
				throw new IllegalArgumentException("Bad or unsupported derive type. This should not happen.");
		}
		
		try {
			newUnit = UnitTools.getUnitWithNameSymbol(newUnit, this.getName(), this.getSymbol());
		} catch(NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		
		return newUnit;
	}
	
	public enum DeriveType {
		multiply,
		divide
	}
}
