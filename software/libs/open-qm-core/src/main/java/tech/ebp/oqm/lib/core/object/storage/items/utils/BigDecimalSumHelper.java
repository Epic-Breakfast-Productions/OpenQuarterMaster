package tech.ebp.oqm.lib.core.object.storage.items.utils;

import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.Unit;
import java.math.BigDecimal;

public class BigDecimalSumHelper extends SumHelper<BigDecimal> {
	
	public BigDecimalSumHelper() {
		super(BigDecimal.valueOf(0.0));
	}
	
	@Override
	public synchronized void add(BigDecimal val) {
		this.setTotal(
			this.getTotal().add(val)
		);
	}
}
