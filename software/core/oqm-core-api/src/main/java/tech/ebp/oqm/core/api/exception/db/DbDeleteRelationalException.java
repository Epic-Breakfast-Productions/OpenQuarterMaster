package tech.ebp.oqm.core.api.exception.db;

import lombok.Getter;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.MainObject;

import java.util.Map;
import java.util.Set;

public class DbDeleteRelationalException extends IllegalStateException {
	
	private static String generateMessage(MainObject object, Map<String, Set<ObjectId>> objectsReferencing) {
		StringBuilder sb = new StringBuilder(object.getClass().getSimpleName())
							   .append(" object with id ")
							   .append(object.getId())
							   .append(" has references to it and could not be deleted. Remove references before deleting. Object(s) that reference it: ");
		
		for(Map.Entry<String, Set<ObjectId>> curClass : objectsReferencing.entrySet()){
			//sb.append("\n");
			sb.append(curClass.getKey())
				.append(": ")
				.append(String.join(", ", curClass.getValue().stream().map(ObjectId::toString).toList()));
		}
		
		return sb.toString();
	}
	
	
	@Getter
	private final Class<?> clazzNotFound;
	@Getter
	private final ObjectId objectId;
	@Getter
	private final Map<String, Set<ObjectId>> objectsReferencing;
	
	public DbDeleteRelationalException(MainObject object, Map<String, Set<ObjectId>> objectsReferencing) {
		super(generateMessage(object, objectsReferencing));
		this.clazzNotFound = object.getClass();
		this.objectId = object.getId();
		this.objectsReferencing = objectsReferencing;
	}
	
}
