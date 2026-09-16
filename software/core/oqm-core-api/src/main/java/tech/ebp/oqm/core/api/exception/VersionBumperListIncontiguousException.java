package tech.ebp.oqm.core.api.exception;

public class VersionBumperListIncontiguousException extends RuntimeException {

	public VersionBumperListIncontiguousException(int versionFrom, Class<?> clazz){
		super("A version bumper could not be found to go from version " + versionFrom + " to " + (versionFrom + 1) + " for class " + clazz.getCanonicalName());
	}
}