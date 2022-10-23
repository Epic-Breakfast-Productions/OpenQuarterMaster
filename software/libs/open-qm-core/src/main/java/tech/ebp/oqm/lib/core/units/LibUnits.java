package tech.ebp.oqm.lib.core.units;

import systems.uom.common.USCustomary;
import tech.units.indriya.unit.Units;

import javax.measure.Unit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class LibUnits {
	
	/**
	 * List of units that are applicable to storage.
	 */
	public static final Map<UnitCategory, Set<Unit<?>>> LIB_UNITS_MAP = new LinkedHashMap<>() {{
		try {
			put(
				UnitCategory.Number,
				Set.of(
					Units.MOLE
				)
			);
			put(
				UnitCategory.Length,
				Set.of(
					Units.METRE,
					UnitTools.getUnitWithNameSymbol(USCustomary.INCH, "Inch", "in"),
					UnitTools.getUnitWithNameSymbol(USCustomary.FOOT, null, "ft"),
					UnitTools.getUnitWithNameSymbol(USCustomary.FOOT_SURVEY, null, "ft"),
					UnitTools.getUnitWithNameSymbol(USCustomary.YARD, null, "yd"),
					UnitTools.getUnitWithNameSymbol(USCustomary.MILE, null, "mi"),
					UnitTools.getUnitWithNameSymbol(USCustomary.NAUTICAL_MILE, null, "nmi")
				)
			);
			put(
				UnitCategory.Mass,
				Set.of(
					UnitTools.getUnitWithNameSymbol(Units.GRAM, "Gram", "g"),
					Units.KILOGRAM,
					UnitTools.getUnitWithNameSymbol(USCustomary.OUNCE, "Ounce", "oz"),
					UnitTools.getUnitWithNameSymbol(USCustomary.POUND, null, "lb"),
					UnitTools.getUnitWithNameSymbol(USCustomary.TON, "Ton", "t")
				)
			);
			put(
				UnitCategory.Area,
				Set.of(
					UnitTools.getUnitWithNameSymbol(Units.SQUARE_METRE, "Meter Squared", Units.SQUARE_METRE.toString()),
					UnitTools.getUnitWithNameSymbol(USCustomary.SQUARE_FOOT, "Square Foot", "ft²"),
					UnitTools.getUnitWithNameSymbol(USCustomary.ARE, null, "a"),
					UnitTools.getUnitWithNameSymbol(USCustomary.HECTARE, null, "ha"),
					UnitTools.getUnitWithNameSymbol(USCustomary.ACRE, null, "ac")
				)
			);
			put(
				UnitCategory.Volume,
				Set.of(
					Units.LITRE,
					UnitTools.getUnitWithNameSymbol(Units.CUBIC_METRE, "Meter Cubed", Units.CUBIC_METRE.toString()),
					//					getUnitWithNameSymbol(USCustomary.LITER, null, USCustomary.LITER.toString()), // same as LITRE
					UnitTools.getUnitWithNameSymbol(USCustomary.CUBIC_INCH, "Cubic Inch", USCustomary.CUBIC_INCH.toString()),
					UnitTools.getUnitWithNameSymbol(USCustomary.CUBIC_FOOT, "Cubic Foot", USCustomary.CUBIC_FOOT.toString()),
					UnitTools.getUnitWithNameSymbol(USCustomary.ACRE_FOOT, null, "ac⋅ft"),
					UnitTools.getUnitWithNameSymbol(USCustomary.BARREL, null, USCustomary.BARREL.toString()),
					UnitTools.getUnitWithNameSymbol(USCustomary.GALLON_DRY, null, "gal dry"),
					UnitTools.getUnitWithNameSymbol(USCustomary.GALLON_LIQUID, null, USCustomary.GALLON_LIQUID.toString()),
					UnitTools.getUnitWithNameSymbol(USCustomary.FLUID_OUNCE, null, USCustomary.FLUID_OUNCE.toString()),
					UnitTools.getUnitWithNameSymbol(USCustomary.GILL_LIQUID, null, USCustomary.GILL_LIQUID.toString()),
					UnitTools.getUnitWithNameSymbol(USCustomary.MINIM, null, "min"),
					UnitTools.getUnitWithNameSymbol(USCustomary.FLUID_DRAM, null, USCustomary.FLUID_DRAM.toString()),
					UnitTools.getUnitWithNameSymbol(USCustomary.CUP, null, USCustomary.CUP.toString()),
					UnitTools.getUnitWithNameSymbol(USCustomary.TEASPOON, null, USCustomary.TEASPOON.toString()),
					UnitTools.getUnitWithNameSymbol(USCustomary.TABLESPOON, null, USCustomary.TABLESPOON.toString()),
					UnitTools.getUnitWithNameSymbol(USCustomary.PINT, null, USCustomary.PINT.toString())
				)
			);
			put(
				UnitCategory.Energy,
				Set.of(
					Units.JOULE,
					Units.VOLT,
					Units.PASCAL,
					Units.WATT
				)
			);
		} catch(NoSuchFieldException | IllegalAccessException e) {
			throw new IllegalStateException("Failed to set name or symbol for Library provided unit(s): " + e.getMessage(), e);
		}
	}};
}
