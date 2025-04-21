package stationCaptainTest.testResources.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.net.URISyntaxException;

@Data
@NoArgsConstructor
public class InstanceConnectionConfig {
	
	private String hostname;
	private String clientId;
	private String clientSecret;
	private int port = 443;
	private String database;
	
	public URI getUri(String service, String endpoint) throws URISyntaxException {
		return new URI("https", null, this.getHostname(), this.getPort(), service + endpoint, null, null);
	}
}
