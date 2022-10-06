package tech.ebp.oqm.lib.core.object.storage.items.utils;

import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.Unit;

public class QuantitySumHelper extends SumHelper<Quantity<?>> {
	
	public QuantitySumHelper(Unit<?> unit) {
		super(Quantities.getQuantity(0, unit));
	}
	
	@Override
	public synchronized void add(Quantity val) {
		this.setTotal(
			this.getTotal().add(val)
		);
	}
}
