package tech.ebp.oqm.baseStation.service.mongo.exception;

import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.object.history.events.DeleteEvent;

/**
 * TODO:: rest mapping
 */
public class DbDeletedException extends DbNotFoundException {
	private static String getMessage(String origMessage){
		return "Object was deleted." + (origMessage==null ? "" : origMessage);
	}
	
	public DbDeletedException(Class<?> clazzNotFound, ObjectId idNotFound, String message) {
		super(clazzNotFound, idNotFound, getMessage(message));
	}
	public DbDeletedException(Class<?> clazzNotFound, ObjectId idNotFound) {
		this(clazzNotFound, idNotFound, (String)null);
	}
	
	public DbDeletedException(Class<?> clazzNotFound, ObjectId idNotFound, String message, Throwable cause) {
		super(clazzNotFound, idNotFound, getMessage(message), cause);
	}
	
	public DbDeletedException(Class<?> clazzNotFound, ObjectId idNotFound, Throwable cause) {
		this(clazzNotFound, idNotFound, null, cause);
	}
	
	public DbDeletedException(DbNotFoundException cause){
		this(cause.getClazzNotFound(), cause.getIdNotFound(), cause);
	}
	
	public DbDeletedException(Class<?> clazzNotFound, DeleteEvent deleteEvent){
		this(clazzNotFound, deleteEvent.getId(), "Deleted at " + deleteEvent.getTimestamp());
	}
}
