package tech.ebp.oqm.core.api.model.object.storage.items.storedWrapper.amountStored;

import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.model.object.history.events.item.ItemLowStockEvent;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.storedWrapper.StoredWrapperTest;
import tech.ebp.oqm.core.api.model.units.OqmProvidedUnits;
import tech.units.indriya.quantity.Quantities;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ListAmountStoredWrapperTest extends StoredWrapperTest {
	
	@Test
	@Override
	public void testLowStockThresholdNoEventOnEmpty() {
		ListAmountStoredWrapper wrapper = new ListAmountStoredWrapper(OqmProvidedUnits.UNIT);
		
		Optional<ItemLowStockEvent> result = wrapper.updateLowStockState();
		
		assertTrue(result.isEmpty());
		assertFalse(wrapper.getNotificationStatus().isLowStock());
	}
	
	@Test
	@Override
	public void testLowStockThresholdNoEventOnExisting() {
		ListAmountStoredWrapper wrapper = new ListAmountStoredWrapper(OqmProvidedUnits.UNIT);
		wrapper.add(new AmountStored(5, OqmProvidedUnits.UNIT));
		
		Optional<ItemLowStockEvent> result = wrapper.updateLowStockState();
		
		assertTrue(result.isEmpty());
		assertFalse(wrapper.getNotificationStatus().isLowStock());
	}
	
	@Test
	@Override
	public void testLowStockThresholdNoEventOnExistingWithThreshold() {
		ListAmountStoredWrapper wrapper = new ListAmountStoredWrapper(OqmProvidedUnits.UNIT);
		wrapper.setLowStockThreshold(Quantities.getQuantity(5, OqmProvidedUnits.UNIT));
		wrapper.add(new AmountStored(5, OqmProvidedUnits.UNIT));
		
		Optional<ItemLowStockEvent> result = wrapper.updateLowStockState();
		
		assertTrue(result.isEmpty());
		assertFalse(wrapper.getNotificationStatus().isLowStock());
	}
	
	@Test
	@Override
	public void testLowStockThreshold() {
		
		ListAmountStoredWrapper wrapper = new ListAmountStoredWrapper(OqmProvidedUnits.UNIT);
		wrapper.setLowStockThreshold(Quantities.getQuantity(6, OqmProvidedUnits.UNIT));
		wrapper.add(new AmountStored(5, OqmProvidedUnits.UNIT));
		
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
		ListAmountStoredWrapper wrapper = new ListAmountStoredWrapper(OqmProvidedUnits.UNIT);
		wrapper.setLowStockThreshold(Quantities.getQuantity(6, OqmProvidedUnits.UNIT));
		wrapper.add(new AmountStored(5, OqmProvidedUnits.UNIT));
		
		Optional<ItemLowStockEvent> result = wrapper.updateLowStockState();
		
		assertTrue(result.isPresent());
		assertTrue(wrapper.getNotificationStatus().isLowStock());
		
		wrapper.addStored(new AmountStored(5, OqmProvidedUnits.UNIT));
		
		result = wrapper.updateLowStockState();
		
		assertTrue(result.isEmpty());
		assertFalse(wrapper.getNotificationStatus().isLowStock());
	}
}