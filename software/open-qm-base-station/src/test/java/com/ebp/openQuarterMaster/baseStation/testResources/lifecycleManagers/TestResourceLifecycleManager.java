package com.ebp.openQuarterMaster.baseStation.testResources.lifecycleManagers;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.jaegertracing.testcontainers.JaegerAllInOne;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.KeysMetadataRepresentation;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.VncRecordingContainer;
import org.testcontainers.lifecycle.TestDescription;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.keycloak.crypto.KeyUse.SIG;
import static org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode.RECORD_ALL;

/**
 * https://www.testcontainers.org/features/networking/
 */
@Slf4j
public class TestResourceLifecycleManager implements QuarkusTestResourceLifecycleManager {
	
	public static final String EXTERNAL_AUTH_ARG = "externalAuth";
	public static final String UI_TEST_ARG = "uiTest";
	public static final String HOST_TESTCONTAINERS_INTERNAL = "host.testcontainers.internal";
	
	private static MongoDbServerManager MONGO_EXE = null;
	private static KeycloakContainer KEYCLOAK_CONTAINER = null;
	private static JaegerAllInOne JAEGER_CONTAINER = null;
	//@Rule //TODO:: play with this in the test classes
	/**
	 * https://www.testcontainers.org/modules/webdriver_containers/
	 */
	private static BrowserWebDriverContainer<?> BROWSER_CONTAINER = null;
	
	static {
		Testcontainers.exposeHostPorts(8081, 8085);
	}
	
	private boolean externalAuth = false;
	private boolean uiTest = false;
	
	public synchronized Map<String, String> startKeycloakTestServer() {
		if (!this.externalAuth) {
			log.info("No need for keycloak.");
			return Map.of();
		}
		if (KEYCLOAK_CONTAINER != null && KEYCLOAK_CONTAINER.isRunning()) {
			log.info("Keycloak already started.");
		} else {
			StopWatch sw = StopWatch.createStarted();
			
			//			Consumer<CreateContainerCmd> cmd = e->e.withPortBindings(new PortBinding(
			//				Ports.Binding.bindPort(80),
			//				new ExposedPort(8085)
			//			));
			//			HostConfig config = new HostConfig().withPortBindings(new PortBinding(Ports.Binding.bindPort(80), new ExposedPort(8085)));
			KEYCLOAK_CONTAINER = new KeycloakContainer()
				//				.withCreateContainerCmdModifier(cmd)
				.withRealmImportFile("keycloak-realm.json");
			log.info("Starting keycloak container with image name: {}", KEYCLOAK_CONTAINER.getDockerImageName());
			KEYCLOAK_CONTAINER.start();
			
			Testcontainers.exposeHostPorts(KEYCLOAK_CONTAINER.getHttpPort());
			
			sw.stop();
			log.info(
				"Started Test Keycloak in {} at endpoint: {}\tAdmin creds: {}:{}",
				sw,
				KEYCLOAK_CONTAINER.getAuthServerUrl(),
				KEYCLOAK_CONTAINER.getAdminUsername(),
				KEYCLOAK_CONTAINER.getAdminPassword()
			);
			
		}
		String clientSecret;
		String publicKey = "";
		try (
			Keycloak keycloak = KeycloakBuilder.builder()
											   .serverUrl(KEYCLOAK_CONTAINER.getAuthServerUrl())
											   .realm("master")
											   .grantType(OAuth2Constants.PASSWORD)
											   .clientId("admin-cli")
											   .username(KEYCLOAK_CONTAINER.getAdminUsername())
											   .password(KEYCLOAK_CONTAINER.getAdminPassword())
											   .build();
		) {
			RealmResource appsRealmResource = keycloak.realms().realm("apps");
			
			ClientRepresentation qmClientResource = appsRealmResource.clients().findByClientId("quartermaster").get(0);
			
			clientSecret = qmClientResource.getSecret();
			
			log.info("Got client id \"{}\" with secret: {}", "quartermaster", clientSecret);
			
			//get private key
			for (KeysMetadataRepresentation.KeyMetadataRepresentation curKey : appsRealmResource.keys().getKeyMetadata().getKeys()) {
				if (!SIG.equals(curKey.getUse())) {
					continue;
				}
				if (!"RSA".equals(curKey.getType())) {
					continue;
				}
				String publicKeyTemp = curKey.getPublicKey();
				if (publicKeyTemp == null || publicKeyTemp.isBlank()) {
					continue;
				}
				publicKey = publicKeyTemp;
				log.info("Found a relevant key for public key use: {} / {}", curKey.getKid(), publicKey);
			}
		}
		// write public key
		File publicKeyFile;
		try {
			publicKeyFile = File.createTempFile("oqmTestKeycloakPublicKey", ".pem");
			log.info("path of public key: {}", publicKeyFile);
			try (
				FileOutputStream os = new FileOutputStream(
					publicKeyFile
				);
			) {
				IOUtils.write(publicKey, os, UTF_8);
			} catch(IOException e) {
				log.error("Failed to write out public key of keycloak: ", e);
				throw new IllegalStateException("Failed to write out public key of keycloak.", e);
			}
		} catch(IOException e) {
			log.error("Failed to create public key file: ", e);
			throw new IllegalStateException("Failed to create public key file", e);
		}
		
		String authServerUrl = KEYCLOAK_CONTAINER.getAuthServerUrl();
		
		if (uiTest) {
			authServerUrl = authServerUrl.replace("localhost", HOST_TESTCONTAINERS_INTERNAL);
		}
		
		String keycloakUrl = authServerUrl.replace("/auth", "");
		
		return Map.of(
			"test.keycloak.port",
			KEYCLOAK_CONTAINER.getHttpPort() + "",
			"test.keycloak.url",
			keycloakUrl,
			"test.keycloak.authUrl",
			authServerUrl,
			"test.keycloak.adminName",
			KEYCLOAK_CONTAINER.getAdminUsername(),
			"test.keycloak.adminPass",
			KEYCLOAK_CONTAINER.getAdminPassword(),
			"service.externalAuth.url",
			keycloakUrl,
			"mp.jwt.verify.publickey.location",
			publicKeyFile.getAbsolutePath(),
			"quarkus.rest-client.keycloak.url",
			"http://localhost:" + KEYCLOAK_CONTAINER.getHttpPort() + "${service.externalAuth.tokenPath:}"
		);
	}
	
	
	public synchronized Map<String, String> startSeleniumWebDriverServer() {
		if (!this.uiTest) {
			log.info("Tests not asking for ui.");
			return Map.of();
		}
		if (BROWSER_CONTAINER == null || !BROWSER_CONTAINER.isRunning()) {
			File recordingDir = new File(
				"build/seleniumRecordings/"
				+ new SimpleDateFormat("yyyyMMddHHmm").format(new Date())
			);
			recordingDir.mkdirs();
			
			BROWSER_CONTAINER = new BrowserWebDriverContainer<>()
				.withCapabilities(new FirefoxOptions())
				.withRecordingMode(
					RECORD_ALL,
					recordingDir,
					VncRecordingContainer.VncRecordingFormat.MP4
				);
			BROWSER_CONTAINER.start();
		}
		return Map.of(
			"runningInfo.hostname",
			HOST_TESTCONTAINERS_INTERNAL
		);
	}
	
	public static synchronized Map<String, String> startJaegerTestServer() {
		if (JAEGER_CONTAINER == null || !JAEGER_CONTAINER.isRunning()) {
			StopWatch sw = StopWatch.createStarted();
			// https://hub.docker.com/r/jaegertracing/all-in-one/tags
			JAEGER_CONTAINER = new JaegerAllInOne("jaegertracing/all-in-one:latest");
			
			JAEGER_CONTAINER.start();
			sw.stop();
			log.info("Started Test Jaeger in {} at: {}", sw, JAEGER_CONTAINER.getQueryPort());
		} else {
			log.info("Jaeger already started.");
		}
		
		return Map.of(
			"quarkus.jaeger.endpoint",
			"http://" + JAEGER_CONTAINER.getContainerIpAddress() + ":" + JAEGER_CONTAINER.getCollectorThriftPort() + "/api/traces"
		);
	}
	
	public static synchronized void stopKeycloakTestServer() {
		if (KEYCLOAK_CONTAINER == null) {
			log.warn("Keycloak was not started.");
			return;
		}
		KEYCLOAK_CONTAINER.stop();
		KEYCLOAK_CONTAINER = null;
	}
	
	public static synchronized void stopSeleniumTestServer() {
		if (BROWSER_CONTAINER == null) {
			log.warn("Web browser was not started.");
			return;
		}
		BROWSER_CONTAINER.stop();
		BROWSER_CONTAINER = null;
	}
	public static synchronized void stopJaegerTestServer() {
		if (JAEGER_CONTAINER == null) {
			log.warn("Jaeger was not started.");
			return;
		}
		JAEGER_CONTAINER.stop();
		JAEGER_CONTAINER = null;
	}
	
	public static WebDriver getWebDriver() {
		if (BROWSER_CONTAINER == null) {
			log.error("No browser started.");
			return null;
		}
		log.info("Getting web driver.");
		try {
			WebDriver driver = BROWSER_CONTAINER.getWebDriver();
			log.info("Got web driver.");
			return driver;
		} catch(Exception e) {
			log.error("Failed to get web driver: ", e);
			throw e;
		}
	}
	
	
	public static void triggerRecord(TestDescription description, Optional<Throwable> throwable) {
		if (BROWSER_CONTAINER != null) {
			log.info("Triggering browser container to save recording of test.");
			BROWSER_CONTAINER.afterTest(description, throwable);
		}
	}
	
	@Override
	public void init(Map<String, String> initArgs) {
		this.externalAuth = Boolean.parseBoolean(initArgs.getOrDefault(EXTERNAL_AUTH_ARG, Boolean.toString(this.externalAuth)));
		this.uiTest = Boolean.parseBoolean(initArgs.getOrDefault(UI_TEST_ARG, Boolean.toString(this.uiTest)));
	}
	
	@Override
	public Map<String, String> start() {
		log.info("STARTING test lifecycle resources.");
		Map<String, String> configOverride = new HashMap<>();
		
		MONGO_EXE = new MongoDbServerManager();
		
		configOverride.putAll(MONGO_EXE.start());
		configOverride.putAll(startKeycloakTestServer());
		configOverride.putAll(startSeleniumWebDriverServer());
		configOverride.putAll(startJaegerTestServer());
		
		log.info("Config overrides: {}", configOverride);
		
		return configOverride;
	}
	
	@Override
	public void stop() {
		log.info("STOPPING test lifecycle resources.");
		MONGO_EXE.stop();
		stopKeycloakTestServer();
		stopSeleniumTestServer();
		stopJaegerTestServer();
	}
}
