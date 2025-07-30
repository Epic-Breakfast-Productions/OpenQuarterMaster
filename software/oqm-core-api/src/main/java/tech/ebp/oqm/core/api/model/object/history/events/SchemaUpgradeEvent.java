package tech.ebp.oqm.core.api.model.object.history.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.object.history.EventType;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;

import static tech.ebp.oqm.core.api.model.object.history.EventType.SCHEMA_UPGRADE;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
public class SchemaUpgradeEvent extends ObjectHistoryEvent {
	
	private String upgradeId;
	private int fromVersion;
	private int toVersion;
	
	public SchemaUpgradeEvent(ObjectId objectId, InteractingEntity entity) {
		super(objectId, entity);
	}
	
	public SchemaUpgradeEvent(MainObject object, InteractingEntity entity) {
		super(object, entity);
	}
	
	
	@Override
	public EventType getType() {
		return SCHEMA_UPGRADE;
	}
}
