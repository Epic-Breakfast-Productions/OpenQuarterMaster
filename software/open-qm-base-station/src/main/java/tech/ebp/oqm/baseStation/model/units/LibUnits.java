package tech.ebp.oqm.baseStation.model.units;

import systems.uom.common.USCustomary;
import tech.ebp.oqm.baseStation.model.units.UnitCategory;
import tech.ebp.oqm.baseStation.model.units.UnitTools;
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
					tech.ebp.oqm.baseStation.model.units.UnitTools.getUnitWithNameSymbol(USCustomary.INCH, "Inch", "in"),
					tech.ebp.oqm.baseStation.model.units.UnitTools.getUnitWithNameSymbol(USCustomary.FOOT, null, "ft"),
					tech.ebp.oqm.baseStation.model.units.UnitTools.getUnitWithNameSymbol(USCustomary.FOOT_SURVEY, null, "ft"),
					tech.ebp.oqm.baseStation.model.units.UnitTools.getUnitWithNameSymbol(USCustomary.YARD, null, "yd"),
					tech.ebp.oqm.baseStation.model.units.UnitTools.getUnitWithNameSymbol(USCustomary.MILE, null, "mi"),
					tech.ebp.oqm.baseStation.model.units.UnitTools.getUnitWithNameSymbol(USCustomary.NAUTICAL_MILE, null, "nmi")
				)
			);
			put(
				UnitCategory.Mass,
				Set.of(
					tech.ebp.oqm.baseStation.model.units.UnitTools.getUnitWithNameSymbol(Units.GRAM, "Gram", "g"),
					Units.KILOGRAM,
					tech.ebp.oqm.baseStation.model.units.UnitTools.getUnitWithNameSymbol(USCustomary.OUNCE, "Ounce", "oz"),
					tech.ebp.oqm.baseStation.model.units.UnitTools.getUnitWithNameSymbol(USCustomary.POUND, null, "lb"),
					tech.ebp.oqm.baseStation.model.units.UnitTools.getUnitWithNameSymbol(USCustomary.TON, "Ton", "t")
				)
			);
			put(
				UnitCategory.Area,
				Set.of(
					tech.ebp.oqm.baseStation.model.units.UnitTools.getUnitWithNameSymbol(Units.SQUARE_METRE, "Meter Squared", Units.SQUARE_METRE.toString()),
					tech.ebp.oqm.baseStation.model.units.UnitTools.getUnitWithNameSymbol(USCustomary.SQUARE_FOOT, "Square Foot", "ft²"),
					tech.ebp.oqm.baseStation.model.units.UnitTools.getUnitWithNameSymbol(USCustomary.ARE, null, "a"),
					tech.ebp.oqm.baseStation.model.units.UnitTools.getUnitWithNameSymbol(USCustomary.HECTARE, null, "ha"),
					tech.ebp.oqm.baseStation.model.units.UnitTools.getUnitWithNameSymbol(USCustomary.ACRE, null, "ac")
				)
			);
			put(
				UnitCategory.Volume,
				Set.of(
					Units.LITRE,
					tech.ebp.oqm.baseStation.model.units.UnitTools.getUnitWithNameSymbol(Units.CUBIC_METRE, "Meter Cubed", Units.CUBIC_METRE.toString()),
					//					getUnitWithNameSymbol(USCustomary.LITER, null, USCustomary.LITER.toString()), // same as LITRE
					tech.ebp.oqm.baseStation.model.units.UnitTools.getUnitWithNameSymbol(USCustomary.CUBIC_INCH, "Cubic Inch", USCustomary.CUBIC_INCH.toString()),
					tech.ebp.oqm.baseStation.model.units.UnitTools.getUnitWithNameSymbol(USCustomary.CUBIC_FOOT, "Cubic Foot", USCustomary.CUBIC_FOOT.toString()),
					tech.ebp.oqm.baseStation.model.units.UnitTools.getUnitWithNameSymbol(USCustomary.ACRE_FOOT, null, "ac⋅ft"),
					tech.ebp.oqm.baseStation.model.units.UnitTools.getUnitWithNameSymbol(USCustomary.BARREL, null, USCustomary.BARREL.toString()),
					tech.ebp.oqm.baseStation.model.units.UnitTools.getUnitWithNameSymbol(USCustomary.GALLON_DRY, null, "gal dry"),
					tech.ebp.oqm.baseStation.model.units.UnitTools.getUnitWithNameSymbol(USCustomary.GALLON_LIQUID, null, USCustomary.GALLON_LIQUID.toString()),
					tech.ebp.oqm.baseStation.model.units.UnitTools.getUnitWithNameSymbol(USCustomary.FLUID_OUNCE, null, USCustomary.FLUID_OUNCE.toString()),
					tech.ebp.oqm.baseStation.model.units.UnitTools.getUnitWithNameSymbol(USCustomary.GILL_LIQUID, null, USCustomary.GILL_LIQUID.toString()),
					tech.ebp.oqm.baseStation.model.units.UnitTools.getUnitWithNameSymbol(USCustomary.MINIM, null, "min"),
					tech.ebp.oqm.baseStation.model.units.UnitTools.getUnitWithNameSymbol(USCustomary.FLUID_DRAM, null, USCustomary.FLUID_DRAM.toString()),
					tech.ebp.oqm.baseStation.model.units.UnitTools.getUnitWithNameSymbol(USCustomary.CUP, null, USCustomary.CUP.toString()),
					tech.ebp.oqm.baseStation.model.units.UnitTools.getUnitWithNameSymbol(USCustomary.TEASPOON, null, USCustomary.TEASPOON.toString()),
					tech.ebp.oqm.baseStation.model.units.UnitTools.getUnitWithNameSymbol(USCustomary.TABLESPOON, null, USCustomary.TABLESPOON.toString()),
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
