package tech.ebp.oqm.core.api.model.object.storage.items;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.model.object.history.events.item.expiry.ItemExpiredEvent;
import tech.ebp.oqm.core.api.model.object.history.events.item.expiry.ItemExpiryEvent;
import tech.ebp.oqm.core.api.model.object.history.events.item.expiry.ItemExpiryWarningEvent;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.testUtils.BasicTest;
import org.apache.commons.lang3.RandomUtils;
import org.bson.types.ObjectId;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public abstract class InventoryItemTest extends BasicTest {
	
	//TODO:: anything?
}
