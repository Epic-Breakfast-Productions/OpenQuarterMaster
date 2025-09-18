package tech.ebp.oqm.lib.core.api.java.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;

public class UriUtils {
	
	public static URI buildUri(URI baseUri, String path, Map<String, String> queryParams) {
		URI uri = baseUri.resolve(path);
		
		if(!queryParams.isEmpty()) {
			StringBuilder sb = new StringBuilder(uri.getQuery() == null ? "" : uri.getQuery());
			
			
			for(Map.Entry<String, String> entry : queryParams.entrySet()) {
				if (!sb.isEmpty()) {
					sb.append('&');
				}
				sb.append(URLEncoder.encode(entry.getKey(), Charset.defaultCharset()));
				sb.append('=');
				sb.append(URLEncoder.encode(entry.getValue(), Charset.defaultCharset()));
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
	
}
