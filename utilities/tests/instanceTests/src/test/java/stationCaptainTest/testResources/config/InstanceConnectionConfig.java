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
	private int keycloakPort = 8115;
	
	public URI getUri(int port, String endpoint) throws URISyntaxException {
		return new URI("https", null, this.getHostname(), port, endpoint, null, null);
	}
}
