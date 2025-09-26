package tech.ebp.oqm.lib.core.api.java;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.lib.core.api.java.testUtils.testClases.JwtAuthTest;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
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
 *
 */
class OqmCoreApiClientHttpsIgnoreTest extends JwtAuthTest {
	
	@BeforeAll
	public static void setup() {
		coreApiContainer.setupForHttps();
		setupAndStart();
	}
	
	
	@Test
	void testBadCertThrowing() throws NoSuchAlgorithmException, KeyManagementException {
		
		OqmCoreApiClient client = OqmCoreApiClient.builder()
									  .config(getCoreApiConfig(true))
									  .build();
		
		CompletionException e = assertThrows(CompletionException.class, () ->client.serverHealthGet().join());
		
		assertInstanceOf(SSLHandshakeException.class, e.getCause());
	}
	
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
		
		OqmCoreApiClient client = OqmCoreApiClient.builder()
									  .httpClient(HttpClient.newBuilder()
													  .sslContext(context)
													  .build())
									  .config(getCoreApiConfig(true))
									  .build();
		
		HttpResponse<ObjectNode> response = client.serverHealthGet().join();
		
		assertEquals(200, response.statusCode(), "Unexpected response code.");
		System.out.println(response.body().toPrettyString());
	}
	
	@Test
	void testSetupClientWithTrustStore() throws NoSuchAlgorithmException, KeyManagementException, IOException, CertificateException, KeyStoreException {
		//build SSLContext to ignore cert issues
		
		KeyStore trustStore = KeyStore.getInstance("PKCS12"); // or "JKS"
		try (FileInputStream instream = new FileInputStream("dev/testKeystore.p12")) {
			trustStore.load(instream, "mypassword".toCharArray());
		}
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(trustStore);
		
		SSLContext context = SSLContext.getInstance("TLS");
		context.init(null, tmf.getTrustManagers(), null);
		
		OqmCoreApiClient client = OqmCoreApiClient.builder()
									  .httpClient(HttpClient.newBuilder()
													  .sslContext(context)
													  .build())
									  .config(getCoreApiConfig(true))
									  .build();
		
		HttpResponse<ObjectNode> response = client.serverHealthGet().join();
		
		assertEquals(200, response.statusCode(), "Unexpected response code.");
		System.out.println(response.body().toPrettyString());
	}
}