package tech.ebp.oqm.baseStation.model.object.history;

import tech.ebp.oqm.baseStation.model.object.history.events.CreateEvent;
import tech.ebp.oqm.baseStation.model.object.history.events.DeleteEvent;
import tech.ebp.oqm.baseStation.model.object.history.events.UpdateEvent;
import tech.ebp.oqm.baseStation.model.object.history.events.file.NewFileVersionEvent;
import tech.ebp.oqm.baseStation.model.object.history.events.item.ItemAddEvent;
import tech.ebp.oqm.baseStation.model.object.history.events.item.ItemSubEvent;
import tech.ebp.oqm.baseStation.model.object.history.events.item.ItemTransferEvent;
import tech.ebp.oqm.baseStation.model.object.history.events.item.expiry.ItemExpiredEvent;
import tech.ebp.oqm.baseStation.model.object.history.events.item.expiry.ItemExpiryWarningEvent;
import tech.ebp.oqm.baseStation.model.object.history.events.user.UserLoginEvent;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntityReference;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntityType;
import tech.ebp.oqm.baseStation.model.testUtils.ObjectSerializationTest;
import tech.ebp.oqm.baseStation.model.units.OqmProvidedUnits;
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

import static tech.ebp.oqm.baseStation.model.object.ObjectUtils.OBJECT_MAPPER;

@Slf4j
@Execution(ExecutionMode.SAME_THREAD)
class HistoryEventSerializationTest extends ObjectSerializationTest<ObjectHistoryEvent> {
	
	private static final Quantity<?> testQuantity = Quantities.getQuantity(20, OqmProvidedUnits.UNIT);
	
	private static JsonNode getRandJsonNode() {
		ObjectNode output = OBJECT_MAPPER.createObjectNode();
		
		return output;
	}
	
	
	HistoryEventSerializationTest() {
		super(ObjectHistoryEvent.class);
	}
	
	public static Stream<Arguments> getObjects() {
		return Stream.of(
			//create
			Arguments.of(new CreateEvent()),
			Arguments.of(new CreateEvent().setEntity(new InteractingEntityReference(ObjectId.get(), InteractingEntityType.USER))),
			//update
			Arguments.of(new UpdateEvent()),
			Arguments.of(new UpdateEvent()
							 .setDescription(FAKER.lorem().paragraph())
							 .setEntity(new InteractingEntityReference(ObjectId.get(), InteractingEntityType.USER))
			),
			//			Arguments.of(UpdateEvent.builder().updateJson(getRandJsonNode()).build()),
			//delete
			Arguments.of(new DeleteEvent()),
			Arguments.of(new DeleteEvent()
							 .setDescription(FAKER.lorem().paragraph())
							 .setEntity(new InteractingEntityReference(ObjectId.get(), InteractingEntityType.USER))
			),
			//login
			Arguments.of(new UserLoginEvent()),
			Arguments.of(new UserLoginEvent().setEntity(new InteractingEntityReference(ObjectId.get(), InteractingEntityType.USER))),
			//item expired
			Arguments.of(new ItemExpiredEvent().setStorageBlockId(ObjectId.get())),
			Arguments.of(
				new ItemExpiredEvent()
					.setIndex(5)
					.setEntity(new InteractingEntityReference(ObjectId.get(), InteractingEntityType.USER))
			),
			Arguments.of(new ItemExpiryWarningEvent().setStorageBlockId(ObjectId.get())),
			Arguments.of(new ItemExpiryWarningEvent()
							 .setStorageBlockId(ObjectId.get())
							 .setIndex(5)
							 .setEntity(new InteractingEntityReference(ObjectId.get(), InteractingEntityType.USER))
			),
			//item low stock
			
			//item add
			Arguments.of(new ItemAddEvent().setStorageBlockId(ObjectId.get()).setQuantity(testQuantity)),
			Arguments.of(
				new ItemAddEvent()
					.setStorageBlockId(ObjectId.get())
					.setQuantity(testQuantity)
					.setDescription((FAKER.lorem().paragraph())
					)
			),
			//item sub
			Arguments.of(new ItemSubEvent().setStorageBlockId(ObjectId.get()).setQuantity(testQuantity)),
			Arguments.of(new ItemSubEvent()
							 .setStorageBlockId(ObjectId.get())
							 .setQuantity(testQuantity)
							 .setDescription(FAKER.lorem().paragraph())
			),
			//item transfer
			Arguments.of(new ItemTransferEvent()
							 .setStorageBlockToId(ObjectId.get())
							 .setStorageBlockFromId(ObjectId.get())
							 .setQuantity(testQuantity)
			),
			Arguments.of(new ItemTransferEvent()
							 .setStorageBlockToId(ObjectId.get())
							 .setStorageBlockFromId(ObjectId.get())
							 .setQuantity(testQuantity)
							 .setDescription(FAKER.lorem().paragraph())
			),
			
			//File Update
			Arguments.of(new NewFileVersionEvent())
		);
	}
	
}