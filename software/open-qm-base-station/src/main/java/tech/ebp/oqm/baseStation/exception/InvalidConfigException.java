package tech.ebp.oqm.baseStation.exception;

/**
 * Exception to convey that the configuration of the running app isn't setup correctly.
 */
public class InvalidConfigException extends IllegalStateException {
	
	public InvalidConfigException() {
	}
	
	public InvalidConfigException(String s) {
		super(s);
	}
	
	public InvalidConfigException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public InvalidConfigException(Throwable cause) {
		super(cause);
	}
	
	public static void assertConfigValNotNullOrBlank(String configVal, String configName){
		if(configVal == null || configVal.isBlank()){
			throw new InvalidConfigException("Value for config \""+configName+"\" not specified.");
		}
	}
}
