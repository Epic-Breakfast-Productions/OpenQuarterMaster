package com.ebp.openQuarterMaster.baseStation.testResources;

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
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.keycloak.crypto.KeyUse.SIG;

/**
 *
 */
@Slf4j
public class TestResourceLifecycleManager implements QuarkusTestResourceLifecycleManager {
    public static final String EXTERNAL_AUTH_ARG = "externalAuth";

    private static MongoDBContainer MONGO_EXE = null;
    private static KeycloakContainer KEYCLOAK_CONTAINER = null;

	private boolean externalAuth = false;

    public synchronized Map<String, String> startKeycloakTestServer() {
        if(!this.externalAuth){
            log.info("No need for keycloak.");
            return Map.of();
        }
        if (KEYCLOAK_CONTAINER != null && KEYCLOAK_CONTAINER.isRunning()) {
            log.info("Keycloak already started.");
        } else {
            StopWatch sw = StopWatch.createStarted();

            KEYCLOAK_CONTAINER = new KeycloakContainer()
                    .withRealmImportFile("keycloak-realm.json");
            KEYCLOAK_CONTAINER.start();

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
            publicKeyFile = File.createTempFile("oqmTestKeycloakPublicKey",".pem");
            log.info("path of public key: {}", publicKeyFile);
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

    public static synchronized Map<String, String> startMongoTestServer() {
        if (MONGO_EXE == null || !MONGO_EXE.isRunning()) {
            StopWatch sw = StopWatch.createStarted();
            MONGO_EXE = new MongoDBContainer(DockerImageName.parse("mongo:5.0.6"));

            MONGO_EXE.start();
            sw.stop();
            log.info("Started Test Mongo in {} at: {}", sw, MONGO_EXE.getReplicaSetUrl());
        } else {
            log.info("Mongo already started.");
        }

        return Map.of(
                "quarkus.mongodb.connection-string", MONGO_EXE.getReplicaSetUrl()
        );
    }

    public static synchronized void stopMongoTestServer() {
        if (MONGO_EXE == null) {
            log.warn("Mongo was not started.");
            return;
        }
        MONGO_EXE.stop();
        MONGO_EXE = null;
    }
    public static synchronized void stopKeycloakTestServer() {
        if (KEYCLOAK_CONTAINER == null) {
            log.warn("Keycloak was not started.");
            return;
        }
        KEYCLOAK_CONTAINER.stop();
        KEYCLOAK_CONTAINER = null;
    }


    @Override
    public void init(Map<String, String> initArgs) {
        this.externalAuth = Boolean.parseBoolean(initArgs.getOrDefault(EXTERNAL_AUTH_ARG, Boolean.toString(this.externalAuth)));
    }

    @Override
    public Map<String, String> start() {
        log.info("STARTING test lifecycle resources.");
        Map<String, String> configOverride = new HashMap<>();

        configOverride.putAll(startMongoTestServer());
        configOverride.putAll(startKeycloakTestServer());

        log.info("Config overrides: {}", configOverride);

        return configOverride;
    }

    @Override
    public void stop() {
//        log.info("STOPPING test lifecycle resources.");
//        stopMongoTestServer();
//        stopKeycloakTestServer();
    }
}
