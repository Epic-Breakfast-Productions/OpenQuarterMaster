package tech.ebp.oqm.lib.core.object.storage.items.storedWrapper;

import tech.ebp.oqm.lib.core.testUtils.BasicTest;

public abstract class StoredWrapperTest extends BasicTest {
	
	
	//TODO:: test expiry, expiry warn fully through
	
	
	public abstract void testLowStockThresholdNoEventOnEmpty();
	
	public abstract void testLowStockThresholdNoEventOnExisting();
	
	public abstract void testLowStockThresholdNoEventOnExistingWithThreshold();
	
	public abstract void testLowStockThreshold();
	
	public abstract void testLowStockThresholdBackToNotLow();
	
	
}