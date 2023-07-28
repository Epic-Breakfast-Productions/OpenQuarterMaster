package tech.ebp.oqm.baseStation.model.objectUpgrade.exception;

public class VersionBumperListIncontiguousException extends Exception {
	
	public VersionBumperListIncontiguousException(int versionFrom, Class<?> clazz){
		super("A version bumper could not be found to go from version " + versionFrom + " to " + (versionFrom + 1) + " for class " + clazz.getCanonicalName());
	}
}
