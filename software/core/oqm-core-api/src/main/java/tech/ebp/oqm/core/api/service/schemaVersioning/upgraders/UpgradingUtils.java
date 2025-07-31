package tech.ebp.oqm.core.api.service.schemaVersioning.upgraders;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class UpgradingUtils {
	
	public static ObjectNode normalizeObjectId(ObjectNode object, String oldField, String newField) {
		if (
			(!object.has(newField) && object.has(oldField)) ||
			(
				oldField.equals(newField) &&
				(object.get(oldField).isObject())
			)
		) {
			object.set(newField, object.get(oldField).get("$oid"));
		}
		
		if (
			!oldField.equals(newField) &&
			object.has(oldField)
		) {
			object.remove(oldField);
		}
		
		return object;
	}
	
	public static ObjectNode normalizeObjectId(ObjectNode object, String field) {
		return normalizeObjectId(object, field, field);
	}
	
	public static ObjectNode normalizeObjectId(ObjectNode object) {
		return normalizeObjectId(object, "_id", "id");
	}
	
	public static ObjectNode dequoteString(ObjectNode object, String field){
		if(object.has(field)) {
			object.put(field, object.get(field).asText().replaceAll("^\"|\"$", ""));
		}
		
		return object;
	}
	
}
