package tech.ebp.oqm.core.api.model.object.storage.items.storedWrapper;

import tech.ebp.oqm.core.api.model.testUtils.BasicTest;

public abstract class StoredWrapperTest extends BasicTest {
	
	
	//TODO:: test expiry, expiry warn fully through
	
	
	public abstract void testLowStockThresholdNoEventOnEmpty();
	
	public abstract void testLowStockThresholdNoEventOnExisting();
	
	public abstract void testLowStockThresholdNoEventOnExistingWithThreshold();
	
	public abstract void testLowStockThreshold();
	
	public abstract void testLowStockThresholdBackToNotLow();
	
	
}