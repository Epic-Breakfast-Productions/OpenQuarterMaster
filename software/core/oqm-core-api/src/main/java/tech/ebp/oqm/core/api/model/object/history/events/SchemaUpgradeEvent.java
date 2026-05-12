package tech.ebp.oqm.core.api.model.object.history.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
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
@Schema(title = "SchemaUpgradeEvent", description = "An event describing how an object's schema was upgraded.")
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
	@Schema(constValue = "SCHEMA_UPGRADE", readOnly = true, required = true, examples = "SCHEMA_UPGRADE")
	public EventType getType() {
		return SCHEMA_UPGRADE;
	}
}
