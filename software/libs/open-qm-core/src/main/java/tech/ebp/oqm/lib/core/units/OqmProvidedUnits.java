package tech.ebp.oqm.lib.core.units;

import tech.units.indriya.unit.Units;

import javax.measure.Unit;
import javax.measure.quantity.AmountOfSubstance;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class OqmProvidedUnits {
	
	public static final Unit<AmountOfSubstance> UNIT;
	public static final Map<UnitCategory, Set<Unit<?>>> OQM_UNITS_MAP;
	
	static {
		try {
			UNIT = UnitTools.getUnitWithNameSymbol(Units.MOLE.divide(6.02214076 * Math.pow(10, 23)), "Units", "units");
		} catch(NoSuchFieldException | IllegalAccessException e) {
			throw new IllegalStateException("Failed to set name or symbol for OQM provided unit(s): " + e.getMessage(), e);
		}
		
		OQM_UNITS_MAP = new LinkedHashMap<>() {{
			put(
				UnitCategory.Number,
				Set.of(
					OqmProvidedUnits.UNIT
				)
			);
		}};
	}
	
	
}
