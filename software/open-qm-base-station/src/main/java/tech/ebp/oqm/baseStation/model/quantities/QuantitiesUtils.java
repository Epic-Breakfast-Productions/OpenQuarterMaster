package tech.ebp.oqm.baseStation.model.quantities;

import javax.measure.Quantity;

public final class QuantitiesUtils {
	
	@SuppressWarnings("rawtypes")
	public static boolean isLowStock(Quantity total, Quantity threshold) {
		if (threshold == null || total == null) {
			return false;
		}
		
		if (!total.getUnit().equals(threshold.getUnit())) {
			threshold = threshold.to(total.getUnit());
		}
		
		return total.getValue().doubleValue() < threshold.getValue().doubleValue();
	}
	
	
}
