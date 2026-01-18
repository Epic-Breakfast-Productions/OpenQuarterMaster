package tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.historyEvent.bumpers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.upgrade.SingleUpgradeResult;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.ObjectSchemaVersionBumper;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.UpgradingUtils;

import java.util.Collection;
import java.util.List;

public class HistoryEventBumper2 extends ObjectSchemaVersionBumper<ObjectHistoryEvent> {
	
	public HistoryEventBumper2() {
		super(2);
	}
	
	private static final Collection<String> availableTypes = List.of(
		"SCHEMA_UPGRADE",
		"CREATE",
		"RECREATE",
		"UPDATE",
		"DELETE",
		"ITEM_EXPIRY_WARNING",
		"ITEM_EXPIRED",
		"ITEM_LOW_STOCK",
		"FILE_NEW_VERSION"
	);
	
	@Override
	protected SingleUpgradeResult bumpObjectSchema(ObjectNode oldObj) {
		SingleUpgradeResult.SingleUpgradeResultBuilder resultBuilder = SingleUpgradeResult.builder()
														.upgradedObject(oldObj);

		if(!availableTypes.contains(oldObj.get("type").asText())){
			resultBuilder.delObj(true);
		} else {
			UpgradingUtils.normalizeObjectId(oldObj, "entity");
			UpgradingUtils.normalizeObjectId(oldObj, "objectId");
			UpgradingUtils.dequoteString(oldObj, "timestamp");
			
			if(!oldObj.has("details")){
				oldObj.putObject("details");
			}
			
			if(oldObj.has("description")){
				ObjectNode note = ((ObjectNode)oldObj.get("details")).putObject("NOTE");
				note.put("type", "NOTE");
				note.put("note", oldObj.get("description").asText());
				
				oldObj.remove("description");
			}
			
			switch(oldObj.get("type").asText()){
				case "UPDATE":
					ObjectNode note = ((ObjectNode)oldObj.get("details")).putObject("FIELDS_AFFECTED");
					note.put("type", "FIELDS_AFFECTED");
					note.set("fieldsUpdated", oldObj.get("fieldsUpdated"));
					
					oldObj.remove("fieldsUpdated");
					break;
			}
		}
		
		return resultBuilder.build();
	}
}
