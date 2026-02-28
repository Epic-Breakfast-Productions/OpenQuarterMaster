package tech.ebp.oqm.lib.core.api.java;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.lib.core.api.java.testUtils.CertUtils;
import tech.ebp.oqm.lib.core.api.java.testUtils.testClases.JwtAuthTest;

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
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * These are examples of how to configure the client when certification issues become a problem.
 */
class OqmCoreApiClientHttpsDemoTest extends JwtAuthTest {
	
	@BeforeAll
	public static void setup() {
		coreApiContainer.setupForHttps();
		setupAndStart();
	}
	
	/**
	 * Shows how a bad cert error will show itself
	 */
	@Test
	void testBadCertThrowing() {
		OqmCoreApiClient client = OqmCoreApiClient.builder()
									  .config(getCoreApiConfig(true).build())
									  .build();
		
		CompletionException e = assertThrows(CompletionException.class, () ->client.serverHealthGet().join());
		
		assertInstanceOf(SSLHandshakeException.class, e.getCause());
	}
	
	/**
	 * Example showing usage of a keystore/truststore containing the OQM instance's certs to properly inform SSL handshakes.
	 *
	 * This is the proper method of resolving cert issues.
	 */
	@Test
	void testSetupClientWithTrustStore() throws NoSuchAlgorithmException, KeyManagementException, IOException, CertificateException, KeyStoreException {
		//load truststore, setup ssl context
		KeyStore trustStore = KeyStore.getInstance("PKCS12"); // or "JKS"
		try (InputStream instream = Files.newInputStream(CertUtils.keystore)) {
			trustStore.load(instream, CertUtils.keystorePass.toCharArray());
		}
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(trustStore);
		
		SSLContext context = SSLContext.getInstance("TLS");
		context.init(null, tmf.getTrustManagers(), null);
		
		//build the client
		OqmCoreApiClient client = OqmCoreApiClient.builder()
									  .httpClient(HttpClient.newBuilder()
													  .sslContext(context)
													  .build())
									  .config(getCoreApiConfig(true).build())
									  .build();
		
		HttpResponse<ObjectNode> response = client.serverHealthGet().join();
		
		assertEquals(200, response.statusCode(), "Unexpected response code.");
		System.out.println(response.body().toPrettyString());
	}
	
	/**
	 * Shows how to build an SSL Context that doesn't care about cert issues
	 *
	 * DO NOT use as standard, only for example. Prefer demonstration of bringing in the server's truststore instead. Use only if opted in by end user.
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	@Test
	void testIgnoreCertIssues() throws NoSuchAlgorithmException, KeyManagementException {
		//build SSLContext to ignore cert issues
		SSLContext context = SSLContext.getInstance("TLS");
		context.init(
			null,
			new TrustManager[]{
				new X509ExtendedTrustManager() {
					public X509Certificate[] getAcceptedIssuers() {
						return new java.security.cert.X509Certificate[0];
					}
					
					public void checkClientTrusted(
						final X509Certificate[] a_certificates,
						final String a_auth_type
					) {
					}
					
					public void checkServerTrusted(
						final X509Certificate[] a_certificates,
						final String a_auth_type
					) {
					}
					
					public void checkClientTrusted(
						final X509Certificate[] a_certificates,
						final String a_auth_type,
						final Socket a_socket
					) {
					}
					
					public void checkServerTrusted(
						final X509Certificate[] a_certificates,
						final String a_auth_type,
						final Socket a_socket
					) {
					}
					
					public void checkClientTrusted(
						final X509Certificate[] a_certificates,
						final String a_auth_type,
						final SSLEngine a_engine
					) {
					}
					
					public void checkServerTrusted(
						final X509Certificate[] a_certificates,
						final String a_auth_type,
						final SSLEngine a_engine
					) {
					}
				}
			},
			null
		);
		
		//build the client
		OqmCoreApiClient client = OqmCoreApiClient.builder()
									  .httpClient(HttpClient.newBuilder()
													  .sslContext(context)
													  .build())
									  .config(getCoreApiConfig(true).build())
									  .build();
		
		HttpResponse<ObjectNode> response = client.serverHealthGet().join();
		
		assertEquals(200, response.statusCode(), "Unexpected response code.");
		System.out.println(response.body().toPrettyString());
	}
	
}