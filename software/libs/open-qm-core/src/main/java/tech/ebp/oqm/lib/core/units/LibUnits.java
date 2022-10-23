package tech.ebp.oqm.lib.core.units;

import systems.uom.common.USCustomary;
import tech.units.indriya.unit.Units;

import javax.measure.Unit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class LibUnits {
	
	/**
	 * List of units that are applicable to storage.
	 */
	public static final Map<String, List<Unit<?>>> ALLOWED_UNITS_MAP = new LinkedHashMap<>() {{
		try {
			put(
				"Number",
				List.of(
					UnitUtils.getUnitWithNameSymbol(OqmProvidedUnits.UNIT, "Units", "units"),
					Units.MOLE
				)
			);
			put(
				"Length",
				List.of(
					Units.METRE,
					UnitUtils.getUnitWithNameSymbol(USCustomary.INCH, "Inch", "in"),
					UnitUtils.getUnitWithNameSymbol(USCustomary.FOOT, null, "ft"),
					UnitUtils.getUnitWithNameSymbol(USCustomary.FOOT_SURVEY, null, "ft"),
					UnitUtils.getUnitWithNameSymbol(USCustomary.YARD, null, "yd"),
					UnitUtils.getUnitWithNameSymbol(USCustomary.MILE, null, "mi"),
					UnitUtils.getUnitWithNameSymbol(USCustomary.NAUTICAL_MILE, null, "nmi")
				)
			);
			put(
				"Mass",
				List.of(
					UnitUtils.getUnitWithNameSymbol(Units.GRAM, "Gram", "g"),
					Units.KILOGRAM,
					UnitUtils.getUnitWithNameSymbol(USCustomary.OUNCE, "Ounce", "oz"),
					UnitUtils.getUnitWithNameSymbol(USCustomary.POUND, null, "lb"),
					UnitUtils.getUnitWithNameSymbol(USCustomary.TON, "Ton", "t")
				)
			);
			put(
				"Area",
				List.of(
					UnitUtils.getUnitWithNameSymbol(Units.SQUARE_METRE, "Meter Squared", Units.SQUARE_METRE.toString()),
					UnitUtils.getUnitWithNameSymbol(USCustomary.SQUARE_FOOT, "Square Foot", "ft²"),
					UnitUtils.getUnitWithNameSymbol(USCustomary.ARE, null, "a"),
					UnitUtils.getUnitWithNameSymbol(USCustomary.HECTARE, null, "ha"),
					UnitUtils.getUnitWithNameSymbol(USCustomary.ACRE, null, "ac")
				)
			);
			put(
				"Volume",
				List.of(
					Units.LITRE,
					UnitUtils.getUnitWithNameSymbol(Units.CUBIC_METRE, "Meter Cubed", Units.CUBIC_METRE.toString()),
					//					getUnitWithNameSymbol(USCustomary.LITER, null, USCustomary.LITER.toString()), // same as LITRE
					UnitUtils.getUnitWithNameSymbol(USCustomary.CUBIC_INCH, "Cubic Inch", USCustomary.CUBIC_INCH.toString()),
					UnitUtils.getUnitWithNameSymbol(USCustomary.CUBIC_FOOT, "Cubic Foot", USCustomary.CUBIC_FOOT.toString()),
					UnitUtils.getUnitWithNameSymbol(USCustomary.ACRE_FOOT, null, "ac⋅ft"),
					UnitUtils.getUnitWithNameSymbol(USCustomary.BARREL, null, USCustomary.BARREL.toString()),
					UnitUtils.getUnitWithNameSymbol(USCustomary.GALLON_DRY, null, "gal dry"),
					UnitUtils.getUnitWithNameSymbol(USCustomary.GALLON_LIQUID, null, USCustomary.GALLON_LIQUID.toString()),
					UnitUtils.getUnitWithNameSymbol(USCustomary.FLUID_OUNCE, null, USCustomary.FLUID_OUNCE.toString()),
					UnitUtils.getUnitWithNameSymbol(USCustomary.GILL_LIQUID, null, USCustomary.GILL_LIQUID.toString()),
					UnitUtils.getUnitWithNameSymbol(USCustomary.MINIM, null, "min"),
					UnitUtils.getUnitWithNameSymbol(USCustomary.FLUID_DRAM, null, USCustomary.FLUID_DRAM.toString()),
					UnitUtils.getUnitWithNameSymbol(USCustomary.CUP, null, USCustomary.CUP.toString()),
					UnitUtils.getUnitWithNameSymbol(USCustomary.TEASPOON, null, USCustomary.TEASPOON.toString()),
					UnitUtils.getUnitWithNameSymbol(USCustomary.TABLESPOON, null, USCustomary.TABLESPOON.toString()),
					UnitUtils.getUnitWithNameSymbol(USCustomary.PINT, null, USCustomary.PINT.toString())
				)
			);
			put(
				"Energy",
				List.of(
					Units.JOULE,
					Units.VOLT,
					Units.PASCAL,
					Units.WATT
				)
			);
		} catch(NoSuchFieldException | IllegalAccessException e) {
			throw new IllegalStateException("Failed to set name or symbol for unit(s): " + e.getMessage(), e);
		}
	}};
	public static final List<Unit<?>> ALLOWED_UNITS = new ArrayList<>();
	public static final Map<Unit<?>, Set<Unit<?>>> UNIT_COMPATIBILITY_MAP = new LinkedHashMap<>();
	
	
	static {
		//flatten map to list
		for (List<Unit<?>> curList : ALLOWED_UNITS_MAP.values()) {
			ALLOWED_UNITS.addAll(curList);
		}
		// build map of compatible units for each unit
		for (Unit<?> curUnit : LibUnits.ALLOWED_UNITS) {
			Set<Unit<?>> compatibleList = new LinkedHashSet<>();
			
			compatibleList.add(curUnit);
			
			for (Unit<?> curComparison : ALLOWED_UNITS) {
				if (curUnit.isCompatible(curComparison)) {
					compatibleList.add(curComparison);
				}
			}
			
			UNIT_COMPATIBILITY_MAP.put(curUnit, compatibleList);
		}
	}
}
