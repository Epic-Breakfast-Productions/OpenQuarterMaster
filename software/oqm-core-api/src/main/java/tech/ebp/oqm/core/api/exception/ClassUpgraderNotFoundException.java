package tech.ebp.oqm.core.api.exception;

public class ClassUpgraderNotFoundException extends Throwable {
	public ClassUpgraderNotFoundException(Class<?> classNotFoundFor){
		super("Could not find upgrader for class " + classNotFoundFor.getCanonicalName());
	}
}
