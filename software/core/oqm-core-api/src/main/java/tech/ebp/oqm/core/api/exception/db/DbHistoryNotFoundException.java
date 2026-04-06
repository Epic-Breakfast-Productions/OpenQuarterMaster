package tech.ebp.oqm.core.api.exception.db;

import org.bson.types.ObjectId;

/**
 * TODO:: rest mapping
 */
public class DbHistoryNotFoundException extends DbNotFoundException {
	
	private static String buildExceptionMessage(Class<?> clazzNotFound, ObjectId idNotFound, String s) {
		return "Could not find history for " + clazzNotFound.getSimpleName() + " with id " + (idNotFound == null ? null :
																								   idNotFound.toHexString()) + (
				   s == null ? "" :
					   " - " + s
			   );
	}
	
	public DbHistoryNotFoundException(String s, Class<?> clazzNotFound, ObjectId idNotFound) {
		super(s, clazzNotFound, idNotFound);
	}
	
	public DbHistoryNotFoundException(Class<?> clazzNotFound, ObjectId idNotFound, String message) {
		super(buildExceptionMessage(clazzNotFound, idNotFound, message), clazzNotFound, idNotFound);
	}
	
	public DbHistoryNotFoundException(Class<?> clazzNotFound, ObjectId idNotFound) {
		super(buildExceptionMessage(clazzNotFound, idNotFound, null), clazzNotFound, idNotFound);
	}
	
	public DbHistoryNotFoundException(Class<?> clazzNotFound, ObjectId idNotFound, String message, Throwable cause) {
		super(buildExceptionMessage(clazzNotFound, idNotFound, message), clazzNotFound, idNotFound, cause);
	}
	
	
}
