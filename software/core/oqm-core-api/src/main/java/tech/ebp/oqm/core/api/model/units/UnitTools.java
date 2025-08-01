package tech.ebp.oqm.core.api.model.units;

import javax.measure.Quantity;
import javax.measure.Unit;
import java.lang.reflect.Field;

public final class UnitTools {

	public static <Q extends Quantity<Q>> Unit<Q> getUnitWithNameSymbol(
		Unit<Q> unit,
		String nameIfNone,
		String symbolIfNone
	) {
		@SuppressWarnings("unchecked")
		Class<? extends Unit<Q>> clazz = (Class<? extends Unit<Q>>) unit.getClass();

		try {
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
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}

		return unit;
	}
}
