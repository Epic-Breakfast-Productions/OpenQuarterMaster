package com.ebp.openQuarterMaster.lib.core;

import systems.uom.common.USCustomary;
import tech.units.indriya.unit.Units;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.AmountOfSubstance;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UnitUtils {
	
	public static final Unit<AmountOfSubstance> UNIT = Units.MOLE.divide(6.02214076 * Math.pow(10, 23));
	
	private static <Q extends Quantity<Q>> Unit<Q> getUnitWithNameSymbol(
		Unit<Q> unit,
		String nameIfNone,
		String symbolIfNone
	) throws NoSuchFieldException, IllegalAccessException {
		@SuppressWarnings("unchecked")
		Class<? extends Unit<Q>> clazz = (Class<? extends Unit<Q>>) unit.getClass();
		
		if (unit.getName() == null || unit.getName().isBlank()) {
			Field f1 = clazz.getSuperclass().getDeclaredField("name");
			f1.setAccessible(true);
			f1.set(unit, nameIfNone);
			f1.setAccessible(false);
		}
		if (unit.getSymbol() == null) {
			Field f1 = clazz.getSuperclass().getDeclaredField("symbol");
			f1.setAccessible(true);
			f1.set(unit, symbolIfNone);
			f1.setAccessible(false);
		}
		
		return unit;
	}
	
	/**
	 * List of units that are applicable to storage.
	 */
	public static final Map<String, List<Unit<?>>> ALLOWED_UNITS_MAP = new LinkedHashMap<>() {{
		try {
			put(
				"Number",
				List.of(
					getUnitWithNameSymbol(UNIT, "Units", "units"),
					Units.MOLE
				)
			);
			put(
				"Length",
				List.of(
					Units.METRE,
					getUnitWithNameSymbol(USCustomary.INCH, "Inch", "in"),
					getUnitWithNameSymbol(USCustomary.FOOT, null, "ft"),
					getUnitWithNameSymbol(USCustomary.FOOT_SURVEY, null, "ft"),
					getUnitWithNameSymbol(USCustomary.YARD, null, "yd"),
					getUnitWithNameSymbol(USCustomary.MILE, null, "mi"),
					getUnitWithNameSymbol(USCustomary.NAUTICAL_MILE, null, "nmi")
				)
			);
			put(
				"Mass",
				List.of(
					getUnitWithNameSymbol(Units.GRAM, "Gram", "g"),
					Units.KILOGRAM,
					getUnitWithNameSymbol(USCustomary.OUNCE, "Ounce", "oz"),
					getUnitWithNameSymbol(USCustomary.POUND, null, "lb"),
					getUnitWithNameSymbol(USCustomary.TON, "Ton", "t")
				)
			);
			put(
				"Area",
				List.of(
					getUnitWithNameSymbol(Units.SQUARE_METRE, "Meter Squared", Units.SQUARE_METRE.toString()),
					getUnitWithNameSymbol(USCustomary.SQUARE_FOOT, "Square Foot", "ft²"),
					getUnitWithNameSymbol(USCustomary.ARE, null, "a"),
					getUnitWithNameSymbol(USCustomary.HECTARE, null, "ha"),
					getUnitWithNameSymbol(USCustomary.ACRE, null, "ac")
				)
			);
			put(
				"Volume",
				List.of(
					Units.LITRE,
					getUnitWithNameSymbol(Units.CUBIC_METRE, "Meter Cubed", Units.CUBIC_METRE.toString()),
//					getUnitWithNameSymbol(USCustomary.LITER, null, USCustomary.LITER.toString()), // same as LITRE
					getUnitWithNameSymbol(USCustomary.CUBIC_INCH, "Cubic Inch", USCustomary.CUBIC_INCH.toString()),
					getUnitWithNameSymbol(USCustomary.CUBIC_FOOT, "Cubic Foot", USCustomary.CUBIC_FOOT.toString()),
					getUnitWithNameSymbol(USCustomary.ACRE_FOOT, null, "ac⋅ft"),
					getUnitWithNameSymbol(USCustomary.BARREL, null, USCustomary.BARREL.toString()),
					getUnitWithNameSymbol(USCustomary.GALLON_DRY, null, "gal dry"),
					getUnitWithNameSymbol(USCustomary.GALLON_LIQUID, null, USCustomary.GALLON_LIQUID.toString()),
					getUnitWithNameSymbol(USCustomary.FLUID_OUNCE, null, USCustomary.FLUID_OUNCE.toString()),
					getUnitWithNameSymbol(USCustomary.GILL_LIQUID, null, USCustomary.GILL_LIQUID.toString()),
					getUnitWithNameSymbol(USCustomary.MINIM, null, "min"),
					getUnitWithNameSymbol(USCustomary.FLUID_DRAM, null, USCustomary.FLUID_DRAM.toString()),
					getUnitWithNameSymbol(USCustomary.CUP, null, USCustomary.CUP.toString()),
					getUnitWithNameSymbol(USCustomary.TEASPOON, null, USCustomary.TEASPOON.toString()),
					getUnitWithNameSymbol(USCustomary.TABLESPOON, null, USCustomary.TABLESPOON.toString()),
					getUnitWithNameSymbol(USCustomary.PINT, null, USCustomary.PINT.toString())
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
		for (Unit<?> curUnit : ALLOWED_UNITS) {
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
	
	public static String stringFromUnit(Unit<?> unit) {
		return unit.toString();
	}
	
	public static Unit<?> unitFromString(String unitStr) {
		for (Unit<?> curUnit : ALLOWED_UNITS) {
			if (stringFromUnit(curUnit).equals(unitStr)) {
				return curUnit;
			}
		}
		return null;
	}
}
