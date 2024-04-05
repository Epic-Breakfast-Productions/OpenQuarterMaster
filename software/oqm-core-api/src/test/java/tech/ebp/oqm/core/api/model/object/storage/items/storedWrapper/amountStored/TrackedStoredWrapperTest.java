package tech.ebp.oqm.core.api.model.object.storage.items.storedWrapper.amountStored;

import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.model.object.history.events.item.ItemLowStockEvent;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.TrackedStored;
import tech.ebp.oqm.core.api.model.object.storage.items.storedWrapper.StoredWrapperTest;
import tech.ebp.oqm.core.api.model.object.storage.items.storedWrapper.trackedStored.TrackedMapStoredWrapper;
import tech.ebp.oqm.core.api.model.units.OqmProvidedUnits;
import tech.units.indriya.quantity.Quantities;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TrackedStoredWrapperTest extends StoredWrapperTest {
	
	@Test
	@Override
	public void testLowStockThresholdNoEventOnEmpty() {
		TrackedMapStoredWrapper wrapper = new TrackedMapStoredWrapper();
		
		Optional<ItemLowStockEvent> result = wrapper.updateLowStockState();
		
		assertTrue(result.isEmpty());
		assertFalse(wrapper.getNotificationStatus().isLowStock());
	}
	
	@Test
	@Override
	public void testLowStockThresholdNoEventOnExisting() {
		TrackedMapStoredWrapper wrapper = new TrackedMapStoredWrapper();
		wrapper.addStored(new TrackedStored(FAKER.idNumber().valid()));
		
		Optional<ItemLowStockEvent> result = wrapper.updateLowStockState();
		
		assertTrue(result.isEmpty());
		assertFalse(wrapper.getNotificationStatus().isLowStock());
	}
	
	@Test
	@Override
	public void testLowStockThresholdNoEventOnExistingWithThreshold() {
		TrackedMapStoredWrapper wrapper = new TrackedMapStoredWrapper();
		wrapper.addStored(new TrackedStored(FAKER.idNumber().valid()));
		wrapper.addStored(new TrackedStored(FAKER.idNumber().valid()));
		wrapper.addStored(new TrackedStored(FAKER.idNumber().valid()));
		wrapper.addStored(new TrackedStored(FAKER.idNumber().valid()));
		wrapper.addStored(new TrackedStored(FAKER.idNumber().valid()));
		wrapper.setLowStockThreshold(Quantities.getQuantity(5, OqmProvidedUnits.UNIT));
		
		Optional<ItemLowStockEvent> result = wrapper.updateLowStockState();
		
		assertTrue(result.isEmpty());
		assertFalse(wrapper.getNotificationStatus().isLowStock());
	}
	
	@Test
	@Override
	public void testLowStockThreshold() {
		TrackedMapStoredWrapper wrapper = new TrackedMapStoredWrapper();
		wrapper.addStored(new TrackedStored(FAKER.idNumber().valid()));
		wrapper.addStored(new TrackedStored(FAKER.idNumber().valid()));
		wrapper.addStored(new TrackedStored(FAKER.idNumber().valid()));
		wrapper.addStored(new TrackedStored(FAKER.idNumber().valid()));
		wrapper.addStored(new TrackedStored(FAKER.idNumber().valid()));
		wrapper.setLowStockThreshold(Quantities.getQuantity(6, OqmProvidedUnits.UNIT));
		
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
		TrackedMapStoredWrapper wrapper = new TrackedMapStoredWrapper();
		wrapper.addStored(new TrackedStored(FAKER.idNumber().valid()));
		wrapper.addStored(new TrackedStored(FAKER.idNumber().valid()));
		wrapper.addStored(new TrackedStored(FAKER.idNumber().valid()));
		wrapper.addStored(new TrackedStored(FAKER.idNumber().valid()));
		wrapper.addStored(new TrackedStored(FAKER.idNumber().valid()));
		wrapper.setLowStockThreshold(Quantities.getQuantity(6, OqmProvidedUnits.UNIT));
		
		Optional<ItemLowStockEvent> result = wrapper.updateLowStockState();
		
		assertTrue(result.isPresent());
		assertTrue(wrapper.getNotificationStatus().isLowStock());
		
		wrapper.addStored(new TrackedStored(FAKER.idNumber().valid()));
		
		result = wrapper.updateLowStockState();
		
		assertTrue(result.isEmpty());
		assertFalse(wrapper.getNotificationStatus().isLowStock());
	}
}