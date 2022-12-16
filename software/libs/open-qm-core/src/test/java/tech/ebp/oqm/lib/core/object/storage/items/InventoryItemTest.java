package tech.ebp.oqm.lib.core.object.storage.items;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.lib.core.object.history.events.item.expiry.ItemExpiredEvent;
import tech.ebp.oqm.lib.core.object.history.events.item.expiry.ItemExpiryEvent;
import tech.ebp.oqm.lib.core.object.history.events.item.expiry.ItemExpiryWarningEvent;
import tech.ebp.oqm.lib.core.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.lib.core.object.storage.items.stored.Stored;
import tech.ebp.oqm.lib.core.testUtils.BasicTest;
import org.apache.commons.lang3.RandomUtils;
import org.bson.types.ObjectId;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public abstract class InventoryItemTest extends BasicTest {
	
	private static final int NUM_ATTS = 50;
	private static final int NUM_KEYWORDS = 25;
	private static final int NUM_IMAGES = 10;
	private static final int NUM_DISTINCT_STORAGE = 500;
	public static final int NUM_STORED = 10_000;
	
	static void fillCommon(InventoryItem<?, ?, ?> item) {
		
		item.setName(FAKER.commerce().productName());
		item.setDescription(FAKER.lorem().paragraph());
		
		for (int i = 0; i < NUM_ATTS; i++) {
			item.getAttributes().put(
				"" + i,
				FAKER.letterify("???" + i)
			);
		}
		
		for (int i = 0; i < NUM_KEYWORDS; i++) {
			item.getKeywords().add(
				FAKER.letterify("???" + i)
			);
		}
		for (int i = 0; i < NUM_IMAGES; i++) {
			item.getImageIds().add(
				ObjectId.get()
			);
		}
	}
	
	public static void fillCommon(Stored stored) {
		stored.setCondition(RandomUtils.nextInt(0, 101));
		stored.setConditionNotes(FAKER.lorem().paragraph());
		stored.setExpires(LocalDateTime.now());
		
		for (int j = 0; j < NUM_IMAGES; j++) {
			stored.getImageIds().add(
				ObjectId.get()
			);
		}
		
		for (int j = 0; j < NUM_ATTS; j++) {
			stored.getAttributes().put(
				"" + j,
				FAKER.letterify("???" + j)
			);
		}
		
		for (int j = 0; j < NUM_KEYWORDS; j++) {
			stored.getKeywords().add(
				FAKER.letterify("???" + j)
			);
		}
	}
	
	public static List<ObjectId> getStorageList() {
		List<ObjectId> output = new ArrayList<>(NUM_DISTINCT_STORAGE);
		
		for (int i = 0; i < NUM_DISTINCT_STORAGE; i++) {
			output.add(ObjectId.get());
		}
		
		return output;
	}
	
	protected static LocalDateTime getNotExpiredExpiryDate() {
		return LocalDateTime.now().plus(30, ChronoUnit.DAYS);
	}
	
	protected static LocalDateTime getExpiredExpiryDate() {
		return LocalDateTime.now().minus(5, ChronoUnit.SECONDS);
	}
	
	protected static Duration getExpiryWarnThreshold() {
		return Duration.of(31, ChronoUnit.DAYS);
	}
	
	/**
	 * Waits 5s before updating, checking
	 *
	 * @param item
	 * @param expectedExpired
	 * @param expectedExpiryWarn
	 *
	 * @throws InterruptedException
	 */
	@ParameterizedTest
	@MethodSource("getExpiryArguments")
	public void testUpdateExpiredStates(
		ListAmountItem item,
		List<AmountStored> expectedExpired,
		List<AmountStored> expectedExpiryWarn
	) {
		List<ItemExpiryEvent> events = item.updateExpiredStates();
		
		List<ItemExpiredEvent> expiredEvents = events.stream()
													 .filter((ItemExpiryEvent e)->{
														 return e instanceof ItemExpiredEvent;
													 }).map((ItemExpiryEvent e)->{
				return (ItemExpiredEvent) e;
			})
													 .collect(Collectors.toList());
		List<ItemExpiryWarningEvent> expiryWarnEvents = events.stream()
															  .filter((ItemExpiryEvent e)->{
																  return e instanceof ItemExpiryWarningEvent;
															  }).map((ItemExpiryEvent e)->{
				return (ItemExpiryWarningEvent) e;
			})
															  .collect(Collectors.toList());
		
		assertEquals(expectedExpired.size(), expiredEvents.size());
		assertEquals(expectedExpiryWarn.size(), expiryWarnEvents.size());
		
		events = item.updateExpiredStates();
		
		expiredEvents = events.stream()
							  .filter((ItemExpiryEvent e)->{
								  return e instanceof ItemExpiredEvent;
							  }).map((ItemExpiryEvent e)->{
				return (ItemExpiredEvent) e;
			})
							  .collect(Collectors.toList());
		expiryWarnEvents = events.stream()
								 .filter((ItemExpiryEvent e)->{
									 return e instanceof ItemExpiryWarningEvent;
								 }).map((ItemExpiryEvent e)->{
				return (ItemExpiryWarningEvent) e;
			})
								 .collect(Collectors.toList());
		
		assertEquals(0, expiredEvents.size());
		assertEquals(0, expiryWarnEvents.size());
	}
	
	//	public abstract void testLowStockEmpty();
	//TODO:: test low stock
}
