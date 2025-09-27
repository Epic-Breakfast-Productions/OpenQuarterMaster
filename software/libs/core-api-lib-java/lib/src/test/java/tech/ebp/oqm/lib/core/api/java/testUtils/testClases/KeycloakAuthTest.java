package tech.ebp.oqm.lib.core.api.java.testUtils.testClases;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import lombok.Getter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import tech.ebp.oqm.lib.core.api.java.auth.KCServiceAccountCreds;
import tech.ebp.oqm.lib.core.api.java.auth.OqmCredentials;
import tech.ebp.oqm.lib.core.api.java.config.CoreApiConfig;
import tech.ebp.oqm.lib.core.api.java.config.KeycloakConfig;
import tech.ebp.oqm.lib.core.api.java.testUtils.JwtUtils;
import tech.ebp.oqm.lib.core.api.java.testUtils.TestContainerUtils;

import java.net.URI;
import java.net.URISyntaxException;

import static tech.ebp.oqm.lib.core.api.java.testUtils.TestContainerUtils.getKeycloakContainer;

public class KeycloakAuthTest extends RunningServerTest {
	
	protected static KeycloakContainer keycloakContainer = TestContainerUtils.getKeycloakContainer();
	
	public static void setupAndStart() {
		keycloakContainer.start();
		coreApiContainer.setupForKcAuth(keycloakContainer);
		startContainers();
	}
	
	@AfterAll
	public static void afterAll() {
		keycloakContainer.stop();
		stopContainers();
	}
	
	public CoreApiConfig.CoreApiConfigBuilder getCoreApiConfig(boolean https, boolean KcDefaultCreds) {
		CoreApiConfig.CoreApiConfigBuilder output = super.getCoreApiConfig(https);
		
		try {
			output.keycloakConfig(
				KeycloakConfig.builder()
					.baseUri(new URI("http://" + keycloakContainer.getHost() + ":" + keycloakContainer.getMappedPort(8080)))
					.clientId("oqm-app")
					.clientSecret("**********")
					.defaultCreds(KcDefaultCreds)
					.build()
			);
		} catch(URISyntaxException e) {
			throw new RuntimeException(e);
		}
		
		return output;
	}
}
