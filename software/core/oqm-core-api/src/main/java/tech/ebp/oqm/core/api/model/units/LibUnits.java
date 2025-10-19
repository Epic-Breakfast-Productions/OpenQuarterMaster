package tech.ebp.oqm.core.api.model.units;

import systems.uom.common.Imperial;
import systems.uom.common.USCustomary;
import tech.units.indriya.unit.Units;

import javax.measure.Unit;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class LibUnits {
	/**
	 * This mostly exists for testing purposes.
	 */
	public static class UnitProxies {
		public static final Unit<Length> METRE = Units.METRE;
		public static final Unit<Length> INCH = UnitTools.getUnitWithNameSymbol(USCustomary.INCH, "Inch", "in");
		public static final Unit<Length> FOOT = UnitTools.getUnitWithNameSymbol(USCustomary.FOOT, null, "ft");
		public static final Unit<Length> FOOT_SURVEY = UnitTools.getUnitWithNameSymbol(USCustomary.FOOT_SURVEY, null, "ft");
		public static final Unit<Length> YARD = UnitTools.getUnitWithNameSymbol(USCustomary.YARD, null, "yd");
		public static final Unit<Length> MILE = UnitTools.getUnitWithNameSymbol(USCustomary.MILE, null, "mi");
		public static final Unit<Length> NAUTICAL_MILE = UnitTools.getUnitWithNameSymbol(USCustomary.NAUTICAL_MILE, null, "nmi");


		public static final Unit<Mass> GRAM = UnitTools.getUnitWithNameSymbol(Units.GRAM, "Gram", "g");
		public static final Unit<Mass> KILOGRAM = Units.KILOGRAM;
		public static final Unit<Mass> OUNCE = UnitTools.getUnitWithNameSymbol(USCustomary.OUNCE, "Ounce", "oz");
		public static final Unit<Mass> POUND = UnitTools.getUnitWithNameSymbol(USCustomary.POUND, null, "lb");
		public static final Unit<Mass> TON = UnitTools.getUnitWithNameSymbol(USCustomary.TON, "Ton", "t_us");

		//TODO:: add rest
	}

	/**
	 * List of units that are applicable to storage.
	 *
	 * https://github.com/unitsofmeasurement/uom-systems/blob/master/common/src/main/java/systems/uom/common/USCustomary.java
	 * https://github.com/unitsofmeasurement/uom-systems/blob/master/common/src/main/java/systems/uom/common/Imperial.java
	 */
	public static final Map<UnitCategory, Set<Unit<?>>> LIB_UNITS_MAP = new LinkedHashMap<>() {{
		put(
			UnitCategory.Number,
			Set.of(
				Units.MOLE
			)
		);
		put(
			UnitCategory.Length,
			Set.of(
				UnitProxies.METRE,
				UnitProxies.INCH,
				UnitProxies.FOOT,
				UnitProxies.FOOT_SURVEY,
				UnitProxies.YARD,
				UnitProxies.MILE,
				UnitProxies.NAUTICAL_MILE
			)
		);
		put(
			UnitCategory.Mass,
			Set.of(
				UnitProxies.GRAM,
				UnitProxies.KILOGRAM,
				UnitProxies.OUNCE,
				UnitProxies.POUND,
				UnitProxies.TON,
				UnitTools.getUnitWithNameSymbol(Imperial.METRIC_TON, "Metric Ton", Imperial.METRIC_TON.toString()),
				UnitTools.getUnitWithNameSymbol(Imperial.TON_UK, "UK Ton", Imperial.TON_UK.toString())
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
				UnitTools.getUnitWithNameSymbol(USCustomary.GALLON_LIQUID.divide(4), "Quart US", "qt_us"),
				UnitTools.getUnitWithNameSymbol(USCustomary.PINT, null, USCustomary.PINT.toString()),
				UnitTools.getUnitWithNameSymbol(USCustomary.FLUID_OUNCE, null, USCustomary.FLUID_OUNCE.toString()),
				UnitTools.getUnitWithNameSymbol(USCustomary.GILL_LIQUID, null, USCustomary.GILL_LIQUID.toString()),
				UnitTools.getUnitWithNameSymbol(USCustomary.MINIM, null, "min"),
				UnitTools.getUnitWithNameSymbol(USCustomary.FLUID_DRAM, null, USCustomary.FLUID_DRAM.toString()),
				UnitTools.getUnitWithNameSymbol(USCustomary.CUP, null, USCustomary.CUP.toString()),
				UnitTools.getUnitWithNameSymbol(USCustomary.TEASPOON, null, USCustomary.TEASPOON.toString()),
				UnitTools.getUnitWithNameSymbol(USCustomary.TABLESPOON, null, USCustomary.TABLESPOON.toString()),
				UnitTools.getUnitWithNameSymbol(Imperial.QUART, null, Imperial.QUART.toString())
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
	}};
}
