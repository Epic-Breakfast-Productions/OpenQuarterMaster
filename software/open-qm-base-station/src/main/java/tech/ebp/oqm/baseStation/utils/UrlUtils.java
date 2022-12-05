package tech.ebp.oqm.baseStation.utils;

import javax.ws.rs.core.UriInfo;

public final class UrlUtils {
	
	public static boolean isUiEndpoint(String endpoint){
		return !endpoint.startsWith("/api") &&
			   !endpoint.startsWith("/q/") &&
			   !endpoint.startsWith("/openapi");
	}
	
	public static boolean isUiEndpoint(UriInfo uriInfo){
		return isUiEndpoint(uriInfo.getPath());
	}
}
