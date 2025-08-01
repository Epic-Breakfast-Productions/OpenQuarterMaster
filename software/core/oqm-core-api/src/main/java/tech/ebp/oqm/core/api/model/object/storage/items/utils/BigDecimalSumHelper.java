package tech.ebp.oqm.core.api.model.object.storage.items.utils;

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
