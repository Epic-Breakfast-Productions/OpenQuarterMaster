package tech.ebp.oqm.core.api.service.schemaVersioning.upgraders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.json.Json;
import tech.ebp.oqm.core.api.exception.UpgradeFailedException;
import tech.ebp.oqm.core.api.model.object.ObjectUtils;

import static tech.ebp.oqm.core.api.model.object.ObjectUtils.OBJECT_MAPPER;

public class UpgradingUtils {
	
	public static ArrayNode normalizeObjectList(ArrayNode list, String bsonInnerField) {
		if(list == null || list.isNull()){
			return list;
		}
		for(int i = 0; i < list.size(); i++){
			JsonNode curNode = list.get(i);
			if(curNode.isObject() && curNode.has(bsonInnerField)){
				list.set(i, curNode.get(bsonInnerField));
			}
		}
		return list;
	}
	
	public static ArrayNode normalizeObjectIdList(ArrayNode list) {
		return normalizeObjectList(list, "$oid");
	}
	
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
	
	
	/**
	 *
	 * @param json
	 * @param field
	 * @param convertedType
	 * @return
	 */
	public static JsonNode stringToConvertedTree(
		ObjectNode json,
		String field,
		Class<?> convertedType
	) {
		if(json.has(field) && json.get(field).isTextual()) {
			try {
				json.set(
					field,
					OBJECT_MAPPER.valueToTree(
						OBJECT_MAPPER.readValue(json.get(field).asText(), convertedType)
					)
				);
			} catch(JsonProcessingException e) {
				throw new UpgradeFailedException(e);
			}
		}
		return json;
	}
}
