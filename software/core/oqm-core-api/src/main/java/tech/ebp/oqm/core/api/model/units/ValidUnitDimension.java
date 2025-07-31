package tech.ebp.oqm.core.api.model.units;

import tech.units.indriya.unit.UnitDimension;

import javax.measure.Dimension;

public enum ValidUnitDimension {
	amount(UnitDimension.AMOUNT_OF_SUBSTANCE),
	length(UnitDimension.LENGTH),
	mass(UnitDimension.MASS),
	electric_current(UnitDimension.ELECTRIC_CURRENT);
	
	public final Dimension dimension;
	
	private ValidUnitDimension(Dimension dimension) {
		this.dimension = dimension;
	}
}
