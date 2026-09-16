package stationCaptainTest.testResources.rest;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import stationCaptainTest.testResources.Utils;
import stationCaptainTest.testResources.config.ConfigReader;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
public class RestHelpers {
	protected static final TrustManager NULL_TRUST_MANAGER = new NullX509TrustManager();
	protected static final SSLContext NULL_SSL_CONTEXT;
	public static final HttpClient.Builder NULL_CERT_TRUST_MANAGER_CLIENT_BUILDER;
	
	static {
		try {
			NULL_SSL_CONTEXT = SSLContext.getInstance("TLS");
			NULL_SSL_CONTEXT.init(null, new TrustManager[]{NULL_TRUST_MANAGER}, new SecureRandom());
		} catch(KeyManagementException | NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		
		NULL_CERT_TRUST_MANAGER_CLIENT_BUILDER = HttpClient.newBuilder()
													 .sslContext(NULL_SSL_CONTEXT);
	}
	
	private static String curClientCredsString = null;
	private static LocalDateTime curCredsExpire = LocalDateTime.now().minusSeconds(10);
	public synchronized static String getClientCredentialString() throws IOException, URISyntaxException, InterruptedException {
		if(curClientCredsString != null && curCredsExpire.isAfter(LocalDateTime.now())){
			return curClientCredsString;
		}
		log.info("Getting new set of client credentials.");
		String authStr = "Basic: " + Base64.getEncoder().encodeToString((ConfigReader.getTestRunConfig().getInstance().getClientId() + ":" + ConfigReader.getTestRunConfig().getInstance().getClientSecret()).getBytes());
		
		HttpClient client = RestHelpers.NULL_CERT_TRUST_MANAGER_CLIENT_BUILDER.build();
		
		byte[] postData = "grant_type=client_credentials".getBytes(StandardCharsets.UTF_8);
		HttpRequest request = HttpRequest.newBuilder()
								  .uri(ConfigReader.getTestRunConfig().getInstance().getUri("/infra/keycloak", "/realms/oqm/protocol/openid-connect/token"))
								  .header("Authorization", authStr)
								  .header("Content-Type", "application/x-www-form-urlencoded")
								  .POST(HttpRequest.BodyPublishers.ofByteArray(postData))
								  .build();
		
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		ObjectNode responseObj = (ObjectNode) Utils.OBJECT_MAPPER.readTree(response.body());
		log.debug("Response from getting new credentials: {}", responseObj);
		String jwt = responseObj.get("access_token").asText();
		
		curClientCredsString = "Bearer " + jwt;
		curCredsExpire = LocalDateTime.now().plusSeconds((responseObj.get("expires_in").asInt() / 3) * 2);
		log.debug("Got new auth token: {}", jwt);
		return curClientCredsString;
	}
	
}
