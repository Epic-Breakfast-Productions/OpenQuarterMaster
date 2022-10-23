package tech.ebp.oqm.lib.core.units;

import tech.units.indriya.unit.Units;

import javax.measure.Unit;
import javax.measure.quantity.AmountOfSubstance;

public final class OqmProvidedUnits {
	
	public static final Unit<AmountOfSubstance> UNIT = Units.MOLE.divide(6.02214076 * Math.pow(10, 23));
}
