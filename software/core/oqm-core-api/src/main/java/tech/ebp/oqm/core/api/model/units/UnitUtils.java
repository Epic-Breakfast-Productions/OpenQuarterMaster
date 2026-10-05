package tech.ebp.oqm.core.api.model.units;

import jakarta.annotation.Nullable;
import lombok.NonNull;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.AmountOfSubstance;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Registry and utility methods for the units supported by the application.
 *
 * <p>Maintains the list of all valid {@link Unit} instances along with two lookup
 * maps: one indexing units by their {@link UnitCategory}, and one mapping each unit
 * to the set of compatible units it can be converted to or from. The registries are
 * seeded on class load with the built-in units from {@link OqmProvidedUnits} and
 * {@link LibUnits}, and can be extended at runtime by registering custom units.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Register a custom unit
 * UnitUtils.registerUnit(UnitCategory.Mass, new MyCustomUnit());
 *
 * // Look up a unit from its string representation
 * Unit<?> unit = UnitUtils.unitFromString("kg");
 *
 * // Check threshold comparisons for quantities
 * boolean under = UnitUtils.atOrUnderThreshold(threshold, quantity);
 * }</pre>
 */
public final class UnitUtils {

	/**
	 * Predefined quantity constants used throughout the application.
	 */
	public static class Quantities {

		/**
		 * A quantity of exactly one unit (see {@link OqmProvidedUnits#UNIT}).
		 */
		public static final Quantity<AmountOfSubstance> UNIT_ONE = tech.units.indriya.quantity.Quantities.getQuantity(1, OqmProvidedUnits.UNIT);
	}

	/**
	 * Ordered list of all registered units, including the built-in units and any
	 * custom units registered at runtime.
	 */
	public static List<Unit<?>> UNIT_LIST;

	/**
	 * Mapping of each {@link UnitCategory} to the set of registered units in that
	 * category.
	 */
	public static Map<UnitCategory, Set<Unit<?>>> UNIT_CATEGORY_MAP;

	/**
	 * Mapping of each registered unit to the set of units it is compatible with
	 * (i.e., can be converted to and from). Every unit is considered compatible
	 * with itself.
	 */
	public static Map<Unit<?>, Set<Unit<?>>> UNIT_COMPATIBILITY_MAP;

	/**
	 * Registers a single unit in the given category, updating the compatibility
	 * mappings for the unit and all existing compatible units.
	 *
	 * <p>Registration is idempotent: registering a unit that is already known is
	 * a no-op.</p>
	 *
	 * @param unitCategory The category the unit belongs to
	 * @param unit The unit to register
	 */
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

		Set<Unit<?>> compatibleUnits = new LinkedHashSet<>();
		compatibleUnits.add(unit);
		for (Map.Entry<Unit<?>, Set<Unit<?>>> cur : UNIT_COMPATIBILITY_MAP.entrySet()) {
			if (cur.getKey().isCompatible(unit)) {
				compatibleUnits.add(cur.getKey());
				cur.getValue().add(unit);
			}
		}
		UNIT_COMPATIBILITY_MAP.put(unit, compatibleUnits);
	}

	/**
	 * Registers a batch of units grouped by category, calling
	 * {@link #registerUnit(UnitCategory, Unit)} for each one.
	 *
	 * @param unitCategoryListMap Mapping of category to the set of units in it
	 */
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

	/**
	 * Registers all units described by the given custom unit entries.
	 *
	 * @param customUnitEntries The custom unit entries to register
	 */
	public static void registerAllUnits(CustomUnitEntry... customUnitEntries) {
		for (CustomUnitEntry customUnitEntry : customUnitEntries) {
			registerUnit(customUnitEntry.getCategory(), customUnitEntry.getUnitCreator().toUnit());
		}
	}

	/**
	 * Registers all units described by the given custom unit entries.
	 *
	 * @param customUnitEntries The custom unit entries to register
	 */
	public static void registerAllUnits(Collection<CustomUnitEntry> customUnitEntries) {
		for (CustomUnitEntry customUnitEntry : customUnitEntries) {
			registerUnit(customUnitEntry.getCategory(), customUnitEntry.getUnitCreator().toUnit());
		}
	}

	/**
	 * Resets the unit registries and re-seeds them with the built-in units from
	 * {@link OqmProvidedUnits} and {@link LibUnits}. Custom units registered
	 * after initialization must be re-registered after calling this method.
	 */
	public static void reInitUnitCollections() {
		UNIT_LIST = new ArrayList<>();

		UNIT_CATEGORY_MAP = new LinkedHashMap<>() {{
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


	/**
	 * Gets the canonical string representation of a unit.
	 *
	 * @param unit The unit to stringify
	 * @return The string form of the unit
	 */
	public static String stringFromUnit(Unit<?> unit) {
		return unit.toString();
	}

	/**
	 * Gets a unit from the string given.
	 *
	 * @param unitStr The string to get the actual unit from
	 * @return The unit the string given represents
	 * @throws IllegalArgumentException If the unit string given is not in the set of valid units, {@link #UNIT_LIST}
	 */
	public static Unit<?> unitFromString(String unitStr) throws IllegalArgumentException {
		for (Unit<?> curUnit : UNIT_LIST) {
			if (stringFromUnit(curUnit).equals(unitStr)) {
				return curUnit;
			}
		}
		throw new IllegalArgumentException("Unit string given (" + unitStr + ") does not represent any of the possible valid units.");
	}

	/**
	 * Determines if a quantity is at or under a threshold.
	 *
	 * @param threshold The threshold to check against. Will return false if null.
	 * @param amount The amount to check if at or under the threshold.
	 * @return If the threshold was not null, and the amount was at or under that threshold.
	 */
	public static boolean atOrUnderThreshold(@Nullable Quantity<?> threshold, Quantity<?> amount) {
		return threshold != null && (((Comparable<Quantity<?>>) amount).compareTo(threshold) <= 0);
	}
}
