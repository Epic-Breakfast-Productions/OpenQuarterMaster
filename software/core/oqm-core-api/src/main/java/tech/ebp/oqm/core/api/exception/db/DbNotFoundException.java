package tech.ebp.oqm.core.api.exception.db;

import lombok.Getter;
import org.bson.types.ObjectId;

/**
 * TODO:: rest mapping
 */
public class DbNotFoundException extends IllegalArgumentException {
	
	private static String buildExceptionMessage(Class<?> clazzNotFound, ObjectId idNotFound, String s) {
		return "Could not find " + clazzNotFound.getSimpleName() + " with id " + (idNotFound == null ? null : idNotFound.toHexString()) + (
			s == null ? "" :
				" - " + s
		);
	}
	
	@Getter
	private final Class<?> clazzNotFound;
	@Getter
	private final ObjectId idNotFound;
	
	public DbNotFoundException(String s, Class<?> clazzNotFound) {
		super(s);
		this.clazzNotFound = clazzNotFound;
		this.idNotFound = null;
	}
	
	public DbNotFoundException(String s, Class<?> clazzNotFound, ObjectId idNotFound) {
		super(s);
		this.clazzNotFound = clazzNotFound;
		this.idNotFound = idNotFound;
	}
	public DbNotFoundException(String s, Class<?> clazzNotFound, ObjectId idNotFound, Throwable cause) {
		super(s, cause);
		this.clazzNotFound = clazzNotFound;
		this.idNotFound = idNotFound;
	}
	
	public DbNotFoundException(Class<?> clazzNotFound, ObjectId idNotFound, String message) {
		super(buildExceptionMessage(clazzNotFound, idNotFound, message));
		this.clazzNotFound = clazzNotFound;
		this.idNotFound = idNotFound;
	}
	
	public DbNotFoundException(Class<?> clazzNotFound, ObjectId idNotFound) {
		this(clazzNotFound, idNotFound, null);
	}
	
	public DbNotFoundException(Class<?> clazzNotFound, ObjectId idNotFound, String message, Throwable cause) {
		super(buildExceptionMessage(clazzNotFound, idNotFound, message), cause);
		this.clazzNotFound = clazzNotFound;
		this.idNotFound = idNotFound;
	}
}
