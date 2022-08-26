package com.ebp.openQuarterMaster.lib.core.object.history.events;

import com.ebp.openQuarterMaster.lib.core.UnitUtils;
import com.ebp.openQuarterMaster.lib.core.object.history.events.item.ItemAddEvent;
import com.ebp.openQuarterMaster.lib.core.object.history.events.item.ItemSubEvent;
import com.ebp.openQuarterMaster.lib.core.object.history.events.item.ItemTransferEvent;
import com.ebp.openQuarterMaster.lib.core.object.history.events.user.UserLoginEvent;
import com.ebp.openQuarterMaster.lib.core.testUtils.ObjectSerializationTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.provider.Arguments;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import java.util.stream.Stream;

@Slf4j
@Execution(ExecutionMode.SAME_THREAD)
class HistoryEventSerializationTest extends ObjectSerializationTest<HistoryEvent> {
	
	private static final Quantity<?> testQuantity = Quantities.getQuantity(20, UnitUtils.UNIT);
	
	private static JsonNode getRandJsonNode() {
		ObjectNode output = OBJECT_MAPPER.createObjectNode();
		
		return output;
	}
	
	
	HistoryEventSerializationTest() {
		super(HistoryEvent.class);
	}
	
	public static Stream<Arguments> getObjects() {
		return Stream.of(
			//create
			Arguments.of(CreateEvent.builder().build()),
			Arguments.of(CreateEvent.builder().userId(ObjectId.get()).build()),
			//update
			Arguments.of(UpdateEvent.builder().build()),
			Arguments.of(UpdateEvent.builder().userId(ObjectId.get()).description(FAKER.lorem().paragraph()).build()),
			//			Arguments.of(UpdateEvent.builder().updateJson(getRandJsonNode()).build()),
			//delete
			Arguments.of(DeleteEvent.builder().build()),
			Arguments.of(DeleteEvent.builder().userId(ObjectId.get()).description(FAKER.lorem().paragraph()).build()),
			//login
			Arguments.of(UserLoginEvent.builder().build()),
			Arguments.of(UserLoginEvent.builder().userId(ObjectId.get()).build()),
			//item add
			Arguments.of(ItemAddEvent.builder().storageBlockId(ObjectId.get()).quantity(testQuantity).build()),
			Arguments.of(ItemAddEvent.builder()
									 .storageBlockId(ObjectId.get())
									 .quantity(testQuantity)
									 .description(FAKER.lorem().paragraph())
									 .build()),
			//item sub
			Arguments.of(ItemSubEvent.builder().storageBlockId(ObjectId.get()).quantity(testQuantity).build()),
			Arguments.of(ItemSubEvent.builder()
									 .storageBlockId(ObjectId.get())
									 .quantity(testQuantity)
									 .description(FAKER.lorem().paragraph())
									 .build()),
			//item transfer
			Arguments.of(ItemTransferEvent.builder()
										  .storageBlockToId(ObjectId.get())
										  .storageBlockFromId(ObjectId.get())
										  .quantity(testQuantity)
										  .build()),
			Arguments.of(ItemTransferEvent.builder()
										  .storageBlockToId(ObjectId.get())
										  .storageBlockFromId(ObjectId.get())
										  .quantity(testQuantity)
										  .description(FAKER.lorem().paragraph())
										  .build())
		);
	}
	
}