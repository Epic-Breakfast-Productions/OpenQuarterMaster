package com.ebp.openQuarterMaster.baseStation.mongoUtils.exception;

import lombok.Getter;
import org.bson.types.ObjectId;

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
