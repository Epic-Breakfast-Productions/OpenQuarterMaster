package tech.ebp.oqm.core.api.service.schemaVersioning.upgraders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import tech.ebp.oqm.core.api.model.object.ObjectUtils;

public class UpgradingUtils {
	
	public static ObjectNode normalizeObject(ObjectNode object, String oldField, String newField, String bsonInnerField) {
		if (
			(!object.has(newField) && object.has(oldField)) ||
			(
				oldField.equals(newField) &&
				(object.get(oldField).isObject())
			)
		) {
			
			if(object.get(oldField).isObject()){
				object.set(newField, object.get(oldField).get(bsonInnerField));
			} else {
				object.set(newField, object.get(oldField));
			}
		}
		
		if (
			!oldField.equals(newField) &&
			object.has(oldField)
		) {
			object.remove(oldField);
		}
		
		return object;
	}
	
	public static ObjectNode normalizeObjectId(ObjectNode object, String oldField, String newField) {
		return normalizeObject(object, oldField, newField, "$oid");
	}
	
	public static ObjectNode normalizeObjectId(ObjectNode object, String field) {
		return normalizeObjectId(object, field, field);
	}
	
	public static ObjectNode normalizeObjectId(ObjectNode object) {
		return normalizeObjectId(object, "_id", "id");
	}
	
	public static ObjectNode dequoteString(ObjectNode object, String field) {
		if (object.has(field)) {
			object.put(field, object.get(field).asText().replaceAll("^\"|\"$", ""));
		}
		
		return object;
	}
	
	public static ObjectNode deserializeJsonField(ObjectNode object, String field) {
		String objectStr = object.get(field).asText();
		
		try {
			object.set(field, ObjectUtils.OBJECT_MAPPER.readTree(objectStr));
		} catch(JsonProcessingException e) {
			throw new RuntimeException("Failed to deserialize a field stored as json: " + field, e);
		}
		
		return object;
	}
}
