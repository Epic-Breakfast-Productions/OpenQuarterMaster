package tech.ebp.oqm.lib.core.api.java;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.lib.core.api.java.config.CoreApiConfig;
import tech.ebp.oqm.lib.core.api.java.testUtils.CertUtils;
import tech.ebp.oqm.lib.core.api.java.testUtils.testClases.JwtAuthTest;
import tech.ebp.oqm.lib.core.api.java.testUtils.testClases.KeycloakAuthTest;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * These are examples of how to configure the client when certification issues become a problem.
 */
class OqmCoreApiClientKeycloakDemoTest extends KeycloakAuthTest {
	
	@BeforeAll
	public static void setup() {
		setupAndStart();
	}
	
	/**
	 * Shows how a bad cert error will show itself
	 */
	@Test
	void testGetSACreds() {
		CoreApiConfig config = this.getCoreApiConfig(false, true).build();
		
		OqmCoreApiClient client = OqmCoreApiClient.builder()
									  .config(config)
									  .build();
		
		System.out.println("Attempting to get SACreds...");
		
		String authHeader = client.getDefaultCreds().getAccessHeaderContent();
		
		System.out.println("Auth Header: " + authHeader);
		
		assertNotNull(authHeader);
	}
	
	@Test
	void testGetSAEntity() {
		CoreApiConfig config = this.getCoreApiConfig(false, true).build();
		OqmCoreApiClient client = OqmCoreApiClient.builder()
									  .config(config)
									  .build();
		
		HttpResponse<String> response = client.interactingEntityGetSelf(client.getDefaultCreds()).join();
		
		System.out.println(response.body());
		String authHeader = client.getDefaultCreds().getAccessHeaderContent();
		
		System.out.println("Auth Header: \"" + authHeader + "\"");
		assertEquals(200, response.statusCode(), "Unexpected response code.");
	}
	
}