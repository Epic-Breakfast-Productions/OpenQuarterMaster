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
