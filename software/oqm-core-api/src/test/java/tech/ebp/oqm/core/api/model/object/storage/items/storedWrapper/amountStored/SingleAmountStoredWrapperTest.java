package tech.ebp.oqm.core.api.model.object.storage.items.storedWrapper.amountStored;

import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.model.object.history.events.item.ItemLowStockEvent;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.storedWrapper.StoredWrapperTest;
import tech.ebp.oqm.core.api.model.units.OqmProvidedUnits;
import tech.units.indriya.quantity.Quantities;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SingleAmountStoredWrapperTest extends StoredWrapperTest {
	
	@Test
	@Override
	public void testLowStockThresholdNoEventOnEmpty() {
		SingleAmountStoredWrapper wrapper = new SingleAmountStoredWrapper(new AmountStored(0, OqmProvidedUnits.UNIT));
		
		Optional<ItemLowStockEvent> result = wrapper.updateLowStockState();
		
		assertTrue(result.isEmpty());
		assertFalse(wrapper.getNotificationStatus().isLowStock());
	}
	
	@Test
	@Override
	public void testLowStockThresholdNoEventOnExisting() {
		SingleAmountStoredWrapper wrapper = new SingleAmountStoredWrapper(new AmountStored(5, OqmProvidedUnits.UNIT));
		
		Optional<ItemLowStockEvent> result = wrapper.updateLowStockState();
		
		assertTrue(result.isEmpty());
		assertFalse(wrapper.getNotificationStatus().isLowStock());
	}
	
	@Test
	@Override
	public void testLowStockThresholdNoEventOnExistingWithThreshold() {
		SingleAmountStoredWrapper
			wrapper =
			(SingleAmountStoredWrapper) new SingleAmountStoredWrapper(new AmountStored(5, OqmProvidedUnits.UNIT))
											.setLowStockThreshold(Quantities.getQuantity(5, OqmProvidedUnits.UNIT));
		
		Optional<ItemLowStockEvent> result = wrapper.updateLowStockState();
		
		assertTrue(result.isEmpty());
		assertFalse(wrapper.getNotificationStatus().isLowStock());
	}
	
	@Test
	@Override
	public void testLowStockThreshold() {
		SingleAmountStoredWrapper
			wrapper =
			(SingleAmountStoredWrapper) new SingleAmountStoredWrapper(new AmountStored(5, OqmProvidedUnits.UNIT))
											.setLowStockThreshold(Quantities.getQuantity(
												6,
												OqmProvidedUnits.UNIT
											));
		
		Optional<ItemLowStockEvent> result = wrapper.updateLowStockState();
		
		assertTrue(result.isPresent());
		assertTrue(wrapper.getNotificationStatus().isLowStock());
		
		
		result = wrapper.updateLowStockState();
		
		assertTrue(result.isEmpty());
		assertTrue(wrapper.getNotificationStatus().isLowStock());
	}
	
	@Test
	@Override
	public void testLowStockThresholdBackToNotLow() {
		SingleAmountStoredWrapper
			wrapper =
			(SingleAmountStoredWrapper) new SingleAmountStoredWrapper(new AmountStored(5, OqmProvidedUnits.UNIT))
											.setLowStockThreshold(Quantities.getQuantity(
												6,
												OqmProvidedUnits.UNIT
											));
		
		Optional<ItemLowStockEvent> result = wrapper.updateLowStockState();
		
		assertTrue(result.isPresent());
		assertTrue(wrapper.getNotificationStatus().isLowStock());
		
		wrapper.addStored(new AmountStored(5, OqmProvidedUnits.UNIT));
		
		result = wrapper.updateLowStockState();
		
		assertTrue(result.isEmpty());
		assertFalse(wrapper.getNotificationStatus().isLowStock());
	}
}