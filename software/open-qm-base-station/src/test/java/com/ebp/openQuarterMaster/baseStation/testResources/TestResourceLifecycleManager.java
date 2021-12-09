package com.ebp.openQuarterMaster.baseStation.testResources;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.KeysMetadataRepresentation;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.keycloak.crypto.KeyUse.SIG;

/**
 * TODO:: make better with configuration
 */
@Slf4j
public class TestResourceLifecycleManager implements QuarkusTestResourceLifecycleManager {
    public static final String EXTERNAL_AUTH_ARG = "externalAuth";

    private static volatile MongodExecutable MONGO_EXE = null;
    private static volatile KeycloakContainer KEYCLOAK_CONTAINER = null;

	private boolean externalAuth = false;

    public synchronized Map<String, String> startKeycloakTestServer() {
        if(!this.externalAuth){
            log.info("No need for keycloak.");
            return Map.of();
        }
        if (KEYCLOAK_CONTAINER != null) {
            log.info("Keycloak already started.");
        } else {
            KEYCLOAK_CONTAINER = new KeycloakContainer()
//				.withEnv("hello","world")
                    .withRealmImportFile("keycloak-realm.json");
            KEYCLOAK_CONTAINER.start();
            log.info(
                    "Test keycloak started at endpoint: {}\tAdmin creds: {}:{}",
                    KEYCLOAK_CONTAINER.getAuthServerUrl(),
                    KEYCLOAK_CONTAINER.getAdminUsername(),
                    KEYCLOAK_CONTAINER.getAdminPassword()
            );

        }
        String clientId;
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
        // = new File(TestResourceLifecycleManager.class.getResource("/").toURI().toString() + "/security/testKeycloakPublicKey.pem");
        File publicKeyFile;
        try {
            publicKeyFile = File.createTempFile("oqmTestKeycloakPublicKey",".pem");
//            publicKeyFile = new File(TestResourceLifecycleManager.class.getResource("/").toURI().toString().replace("/classes/java/", "/resources/") + "/security/testKeycloakPublicKey.pem");
            log.info("path of public key: {}", publicKeyFile);
//            if(publicKeyFile.createNewFile()){
//                log.info("created new public key file");
//
//            } else {
//                log.info("Public file already exists");
//            }
            try (
                    FileOutputStream os = new FileOutputStream(
                            publicKeyFile
                    );
            ) {
                IOUtils.write(publicKey, os, UTF_8);
            } catch (IOException e) {
                log.error("Failed to write out public key of keycloak: ", e);
                throw new IllegalStateException("Failed to write out public key of keycloak.", e);
            }
        } catch (IOException  e) {
            log.error("Failed to create public key file: ", e);
            throw new IllegalStateException("Failed to create public key file", e);
        }

        String keycloakUrl = KEYCLOAK_CONTAINER.getAuthServerUrl().replace("/auth", "");

        return Map.of(
                "test.keycloak.url", keycloakUrl,
                "test.keycloak.authUrl", KEYCLOAK_CONTAINER.getAuthServerUrl(),
                "test.keycloak.adminName", KEYCLOAK_CONTAINER.getAdminUsername(),
                "test.keycloak.adminPass", KEYCLOAK_CONTAINER.getAdminPassword(),
                "service.externalAuth.url", keycloakUrl,
                "mp.jwt.verify.publickey.location", publicKeyFile.getAbsolutePath()


        );
    }

    public static synchronized void startMongoTestServer() throws IOException {
        if (MONGO_EXE != null) {
            log.info("Flapdoodle Mongo already started.");
            return;
        }
        Version.Main version = Version.Main.V4_0;
        int port = 27018;
        log.info("Starting Flapdoodle Test Mongo {} on port {}", version, port);
        IMongodConfig config = new MongodConfigBuilder()
                .version(version)
                .net(new Net(port, Network.localhostIsIPv6()))
                .build();
        try {
            MONGO_EXE = MongodStarter.getDefaultInstance().prepare(config);
            MongodProcess process = MONGO_EXE.start();
            if (!process.isProcessRunning()) {
                throw new IOException();
            }
        } catch (Throwable e) {
            log.error("FAILED to start test mongo server: ", e);
            MONGO_EXE = null;
            throw e;
        }
    }

    public static synchronized void stopMongoTestServer() {
        if (MONGO_EXE == null) {
            log.warn("Mongo was not started.");
            return;
        }
        MONGO_EXE.stop();
        MONGO_EXE = null;
    }

    public synchronized static void cleanMongo() throws IOException {
        if (MONGO_EXE == null) {
            log.warn("Mongo was not started.");
            return;
        }

        log.info("Cleaning Mongo of all entries.");
    }


    @Override
    public void init(Map<String, String> initArgs) {
        this.externalAuth = Boolean.parseBoolean(initArgs.getOrDefault(EXTERNAL_AUTH_ARG, Boolean.toString(this.externalAuth)));
    }

    @Override
    public Map<String, String> start() {
        log.info("STARTING test lifecycle resources.");
        Map<String, String> configOverride = new HashMap<>();
        try {
            startMongoTestServer();
        } catch (IOException e) {
            log.error("Unable to start Flapdoodle Mongo server");
        }

        configOverride.putAll(startKeycloakTestServer());

        return configOverride;
    }

    @Override
    public void stop() {
        log.info("STOPPING test lifecycle resources.");
        stopMongoTestServer();
    }
}
