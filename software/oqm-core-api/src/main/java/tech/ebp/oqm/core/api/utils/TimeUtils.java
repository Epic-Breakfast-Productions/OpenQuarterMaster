package tech.ebp.oqm.core.api.utils;

public class TimeUtils {
	
	/**
	 * @return the current time in seconds since epoch
	 */
	public static long currentTimeInSecs() {
		long currentTimeMS = System.currentTimeMillis();
		return (currentTimeMS / 1000L);
	}
}
