package stationCaptainTest.testResources;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Scenario;
import lombok.Data;
import stationCaptainTest.testResources.config.ConfigReader;
import stationCaptainTest.testResources.config.TestRunConfig;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import java.io.IOException;
import java.net.Socket;
import java.net.http.HttpClient;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

@Data
//@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseStepDefinitions {
	protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	protected static final TestRunConfig CONFIG;
	protected static final TrustManager NULL_TRUST_MANAGER = new X509ExtendedTrustManager() {
		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[]{};
		}
		
		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) {
		}
		
		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) {
		}
		
		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) {
		}
		
		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) {
		}
		
		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
		}
		
		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
		}
	};
	protected static final HttpClient.Builder NULL_CERT_TRUST_MANAGER_CLIENT_BUILDER;
	
	static {
		SSLContext sslContext = null;
		try {
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, new TrustManager[]{NULL_TRUST_MANAGER}, new SecureRandom());
		} catch(KeyManagementException | NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		
		NULL_CERT_TRUST_MANAGER_CLIENT_BUILDER = HttpClient.newBuilder()
													 .sslContext(sslContext);
				
//				HttpClients.custom()
//														 .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
//																				   .setSSLSocketFactory(SSLConnectionSocketFactoryBuilder.create()
//																											.setSslContext(SSLContextBuilder.create()
//																															   .loadTrustMaterial(TrustAllStrategy.INSTANCE)
//																															   .build())
//																											.setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
//																											.build())
//																				   .build());
	
	}
	
	
	
	
	
	
	
	
	static {
		try {
			CONFIG = ConfigReader.getTestRunConfig();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Scenario scenario;
	private TestContext context;
	
	protected BaseStepDefinitions(TestContext context){
		this.context = context;
	}
	
	public abstract void setup(Scenario scenario);
}
