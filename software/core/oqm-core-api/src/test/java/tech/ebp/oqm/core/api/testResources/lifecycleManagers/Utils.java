package tech.ebp.oqm.core.api.testResources.lifecycleManagers;

public class Utils {
	
	public static final String HOST_TESTCONTAINERS_INTERNAL = "host.testcontainers.internal";
	public static final String HOST_DOCKER_INTERNAL = "host.docker.internal";
	
	public static String replaceLocalWithTCInternal(String original){
		return original.replace("localhost", HOST_TESTCONTAINERS_INTERNAL);
	}
	public static String replaceLocalWithTCInternalIf(boolean ifTrue, String original){
		if(ifTrue){
			return replaceLocalWithTCInternal(original);
		}
		return original;
	}
	
	public static String replaceLocalWithDockerInternal(String original){
		return original.replace("localhost", HOST_DOCKER_INTERNAL);
	}
	public static String replaceLocalWithDockerInternalIf(boolean ifTrue, String original){
		if(ifTrue){
			return replaceLocalWithDockerInternal(original);
		}
		return original;
	}
}
