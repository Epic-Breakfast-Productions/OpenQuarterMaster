package tech.ebp.oqm.baseStation.testResources.lifecycleManagers;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.KeysMetadataRepresentation;
import org.testcontainers.Testcontainers;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.keycloak.crypto.KeyUse.SIG;
import static tech.ebp.oqm.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager.EXTERNAL_AUTH_ARG;
import static tech.ebp.oqm.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager.UI_TEST_ARG;

@Slf4j
public class KeycloakServerManager implements QuarkusTestResourceLifecycleManager {
	
	private static KeycloakContainer KEYCLOAK_CONTAINER = null;
	
	private boolean externalAuth = false;
	private boolean uiTest = false;
	
	@Override
	public Map<String, String> start() {
		if(!externalAuth){
			log.info("Tests not calling for external auth.");
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
		authServerUrl = Utils.replaceLocalWithTCInternalIf(uiTest, authServerUrl);
		
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
	
	@Override
	public void stop() {
		if (KEYCLOAK_CONTAINER == null) {
			log.warn("Keycloak was not started.");
			return;
		}
		KEYCLOAK_CONTAINER.stop();
		KEYCLOAK_CONTAINER = null;
	}
	
	@Override
	public void init(Map<String, String> initArgs) {
		QuarkusTestResourceLifecycleManager.super.init(initArgs);
		this.externalAuth = Boolean.parseBoolean(initArgs.getOrDefault(EXTERNAL_AUTH_ARG, Boolean.toString(this.externalAuth)));
		this.uiTest = Boolean.parseBoolean(initArgs.getOrDefault(UI_TEST_ARG, Boolean.toString(this.uiTest)));
	}
}
