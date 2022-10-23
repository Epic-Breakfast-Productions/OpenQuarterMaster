package tech.ebp.oqm.lib.core.units;

import lombok.NonNull;

import javax.measure.Quantity;
import javax.measure.Unit;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class UnitUtils {
	
	/**
	 * List of all units we support and deal with.
	 */
	public static List<Unit<?>> UNIT_LIST;
	
	public static Map<UnitCategory, Set<Unit<?>>> UNIT_CATEGORY_MAP;
	
	public static Map<Unit<?>, Set<Unit<?>>> UNIT_COMPATIBILITY_MAP;
	
	
	public static void registerUnit(
		@NonNull UnitCategory unitCategory,
		@NonNull Unit<?> unit
	) {
		if (UNIT_LIST.contains(unit)) {
			return;
		}
		//TODO:: figure out validation to not step on existing units
		
		UNIT_LIST.add(unit);
		
		UNIT_CATEGORY_MAP.get(unitCategory).add(unit);
		
		Set<Unit<?>> compatibleUnits = new HashSet<>();
		for (Map.Entry<Unit<?>, Set<Unit<?>>> cur : UNIT_COMPATIBILITY_MAP.entrySet()) {
			if (cur.getKey().isCompatible(unit)) {
				compatibleUnits.add(cur.getKey());
				cur.getValue().add(unit);
			}
		}
		UNIT_COMPATIBILITY_MAP.put(unit, compatibleUnits);
	}
	
	public static void registerAllUnits(
		@NonNull Map<UnitCategory, Set<Unit<?>>> unitCategoryListMap
	) {
		unitCategoryListMap.forEach((UnitCategory curCategory, Collection<Unit<?>> units)->{
			units.forEach((Unit<?> curUnit)->{
				registerUnit(
					curCategory,
					curUnit
				);
			});
		});
	}
	
	public static void registerAllUnits(CustomUnitEntry... customUnitEntries) {
		for (CustomUnitEntry customUnitEntry : customUnitEntries) {
			registerUnit(customUnitEntry.getCategory(), customUnitEntry.getUnit());
		}
	}
	
	public static void registerAllUnits(Collection<CustomUnitEntry> customUnitEntries) {
		for (CustomUnitEntry customUnitEntry : customUnitEntries) {
			registerUnit(customUnitEntry.getCategory(), customUnitEntry.getUnit());
		}
	}
	
	public static void reInitUnitCollections() {
		UNIT_LIST = new ArrayList<>();
		
		UNIT_CATEGORY_MAP = new HashMap<>() {{
			for (UnitCategory curCat : UnitCategory.values()) {
				this.put(curCat, new LinkedHashSet<>());
			}
		}};
		
		UNIT_COMPATIBILITY_MAP = new LinkedHashMap<>();
		
		registerAllUnits(OqmProvidedUnits.OQM_UNITS_MAP);
		registerAllUnits(LibUnits.LIB_UNITS_MAP);
	}
	
	static {
		reInitUnitCollections();
	}
	
	
	static <Q extends Quantity<Q>> Unit<Q> getUnitWithNameSymbol(
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
	
	public static String stringFromUnit(Unit<?> unit) {
		return unit.toString();
	}
	
	/**
	 * Gets a unit from the string given.
	 *
	 * @param unitStr The string to get the actual unit from
	 *
	 * @return The unit the string given represents
	 * @throws IllegalArgumentException If the unit string given is not in the set of valid units, {@link #UNIT_LIST}
	 */
	public static Unit<?> unitFromString(String unitStr) throws IllegalArgumentException {
		for (Unit<?> curUnit : UNIT_LIST) {
			if (stringFromUnit(curUnit).equals(unitStr)) {
				return curUnit;
			}
		}
		throw new IllegalArgumentException("Unit string given does not represent any of the possible valid units.");
	}
}
