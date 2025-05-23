package tech.ebp.oqm.core.api.model.object.history.events.externalService;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.object.history.EventType;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
//@SuperBuilder
@BsonDiscriminator
public class ExtServiceSetupEvent extends ObjectHistoryEvent {
	public static final int CUR_SCHEMA_VERSION = 1;
	
	public ExtServiceSetupEvent(ObjectId objectId, InteractingEntity entity) {
		super(objectId, entity);
	}
	
	public ExtServiceSetupEvent(MainObject object, InteractingEntity entity) {
		super(object, entity);
	}
	
	@Override
	public EventType getType() {
		return EventType.EXT_SERVICE_SETUP;
	}

	@Override
	public int getSchemaVersion() {
		return CUR_SCHEMA_VERSION;
	}
}
