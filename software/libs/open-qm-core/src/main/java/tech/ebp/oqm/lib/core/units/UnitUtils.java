package tech.ebp.oqm.lib.core.units;

import javax.measure.Quantity;
import javax.measure.Unit;
import java.lang.reflect.Field;

public class UnitUtils {
	
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
	 * @throws IllegalArgumentException If the unit string given is not in the set of valid units, {@link LibUnits#ALLOWED_UNITS}
	 */
	public static Unit<?> unitFromString(String unitStr) throws IllegalArgumentException {
		for (Unit<?> curUnit : LibUnits.ALLOWED_UNITS) {
			if (stringFromUnit(curUnit).equals(unitStr)) {
				return curUnit;
			}
		}
		throw new IllegalArgumentException("Unit string given does not represent any of the possible valid units.");
	}
}
