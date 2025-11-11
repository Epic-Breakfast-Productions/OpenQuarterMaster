package tech.ebp.oqm.core.api.model.units;

import systems.uom.common.USCustomary;
import tech.units.indriya.unit.Units;

import javax.measure.Unit;
import javax.measure.quantity.AmountOfSubstance;
import javax.measure.quantity.Volume;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class OqmProvidedUnits {

	public static final Unit<AmountOfSubstance> UNIT = UnitTools.getUnitWithNameSymbol(Units.MOLE.divide(6.02214076 * Math.pow(10, 23)), "Units", "units");
	public static final Unit<?> WATT_HOURS = UnitTools.getUnitWithNameSymbol(Units.WATT.multiply(Units.HOUR), "Watt-Hour", "Wh");
	public static final Unit<?> MILLI_WATT_HOURS = UnitTools.getUnitWithNameSymbol(WATT_HOURS.divide(1_000), "Milliwatt-Hour", "mWh");
	

	public static final Map<UnitCategory, Set<Unit<?>>> OQM_UNITS_MAP;

	static {
		OQM_UNITS_MAP = new LinkedHashMap<>() {{
			put(
				UnitCategory.Number,
				Set.of(
					OqmProvidedUnits.UNIT
				)
			);
			put(
				UnitCategory.Energy,
				Set.of(
					OqmProvidedUnits.WATT_HOURS,
					OqmProvidedUnits.MILLI_WATT_HOURS
				)
			);
		}};
	}


}
