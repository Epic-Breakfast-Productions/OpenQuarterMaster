package tech.ebp.oqm.lib.core.api.java.utils;

import lombok.NoArgsConstructor;
import tech.ebp.oqm.lib.core.api.java.search.ListParamVal;
import tech.ebp.oqm.lib.core.api.java.search.QParamVal;
import tech.ebp.oqm.lib.core.api.java.search.QueryParams;
import tech.ebp.oqm.lib.core.api.java.search.StringParamVal;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Utils related to uris
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class UriUtils {
	
	private static void appendQueryParam(StringBuilder sb, String key, String val) {
		sb.append(urlEncode(key));
		sb.append('=');
		sb.append(urlEncode(val));
	};
	
	/**
	 * Builds a url from the parameters.
	 * @param baseUri The base URI to tack onto
	 * @param path The path under the base URI to resolve
	 * @param queryParams Query parameters, if any, to attach to the URI
	 * @return The new URI
	 */
	public static URI buildUri(URI baseUri, String path, QueryParams queryParams) {
		URI uri = baseUri.resolve(path);
		
		if (!queryParams.isEmpty()) {
			StringBuilder sb = new StringBuilder(uri.getQuery() == null ? "" : uri.getQuery());
			
			
			for (Map.Entry<String, QParamVal<?>> entry : queryParams.entrySet()) {
				if (!sb.isEmpty()) {
					sb.append('&');
				}
				
				if(entry.getValue() instanceof StringParamVal) {
					appendQueryParam(sb, entry.getKey(), ((StringParamVal) entry.getValue()).get());
				} else if (entry.getValue() instanceof ListParamVal){
					boolean first = true;
					for(String val : ((ListParamVal) entry.getValue()).get()){
						if(first){
							first = false;
						} else {
							sb.append('&');
						}
						appendQueryParam(sb, entry.getKey(), val);
					}
				}
			}
			
			try {
				uri = new URI(
					uri.getScheme(),
					uri.getAuthority(),
					uri.getPath(),
					sb.toString(),
					uri.getFragment()
				);
			} catch(URISyntaxException e) {
				throw new RuntimeException("Failed to build exception for request.", e);
			}
		}
		
		return uri;
	}
	
	/**
	 * URL encodes a string using the default charset.
	 * @param str The string to encode.
	 * @return The encoded string.
	 */
	public static String urlEncode(String str) {
		return URLEncoder.encode(str, Charset.defaultCharset());
	}
	
}
