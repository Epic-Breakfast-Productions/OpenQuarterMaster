package tech.ebp.oqm.lib.core.api.java.utils;

import lombok.NoArgsConstructor;

/**
 * Constants for use throughout the client.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class Constants {
	
	/** The top level api v1 path */
	public static final String API_V1_PATH = "/api/v1";
	/** The top level api v1 path for a particular database. */
	public static final String API_V1_DB_PATH = API_V1_PATH + "/db/[db]";

}
