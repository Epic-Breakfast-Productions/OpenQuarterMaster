package tech.ebp.oqm.lib.core.api.quarkus.deployment;

import com.github.dockerjava.api.model.HostConfig;
import io.quarkus.deployment.IsLocalDevelopment;
import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CuratedApplicationShutdownBuildItem;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.builditem.RunTimeConfigurationDefaultBuildItem;
import io.quarkus.deployment.builditem.Startable;
import io.quarkus.deployment.dev.devservices.DevServicesConfig;
import io.quarkus.devservices.common.ConfigureUtil;
import io.quarkus.devui.spi.JsonRPCProvidersBuildItem;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.Page;
import io.quarkus.smallrye.health.deployment.spi.HealthBuildItem;
import org.jboss.logging.Logger;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;
import tech.ebp.oqm.lib.core.api.quarkus.deployment.config.CoreApiLibBuildTimeConfig;
import tech.ebp.oqm.lib.core.api.quarkus.deployment.testContainers.OqmCoreApiWebServiceContainer;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.Constants;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Processes runtime features like configs, health checks, and devservices
 */
class CoreApiLibQuarkusProcessor {

	private static final Logger log = Logger.getLogger(CoreApiLibQuarkusProcessor.class);

	private static final String FEATURE = "core-api-lib-quarkus";
	private static final String MONGODB_DEVSERVICE_HOSTNAME = "oqm-core-api-mongodb";
	private static final String HOST = "host.testcontainers.internal";
	private static final String KEYCLOAK_DEVSERVICE_HOSTNAME = HOST; //TODO: #1287 should not use this in non-host netowrking
	private static final String KAFKA_DEVSERVICE_HOSTNAME = "localhost"; //TODO: #1287 should not use this in non-host netowrking

	private static volatile boolean firstSetup = true;

	private static volatile Map<String, DevServicesResultBuildItem> DEVSERVICES = new HashMap<>();


	@BuildStep
	FeatureBuildItem feature() {
		return new FeatureBuildItem(FEATURE);
	}

	@BuildStep
	List<RunTimeConfigurationDefaultBuildItem> addRestConfiguration() {
		return List.of(
			new RunTimeConfigurationDefaultBuildItem("quarkus.rest-client.\"" + Constants.CORE_API_CLIENT_NAME + "\".url", "${" + Constants.CONFIG_ROOT_NAME + ".baseUri}"),
			new RunTimeConfigurationDefaultBuildItem("quarkus.rest-client.\"" + Constants.CORE_API_CLIENT_OIDC_NAME + "\".url", "${quarkus.oidc.auth-server-url:}")
		);
	}

	@BuildStep
	HealthBuildItem addHealthCheck(CoreApiLibBuildTimeConfig buildTimeConfig) {
		return new HealthBuildItem("tech.ebp.oqm.lib.core.api.quarkus.runtime.health.CoreApiHealthCheck", buildTimeConfig.health().enabled());
	}

	private MongoDBContainer newMongoDbContainer() {
		log.info("Starting new MongoDB dev container");
		DockerImageName mongoImageName = DockerImageName.parse("mongo:7");

		MongoDBContainer mongoDBContainer = new MongoDBContainer(mongoImageName);
		//		mongoDBContainer.addExposedPorts();

		ConfigureUtil.configureSharedNetwork(mongoDBContainer, MONGODB_DEVSERVICE_HOSTNAME);

		mongoDBContainer.withNetworkAliases(MONGODB_DEVSERVICE_HOSTNAME);
		mongoDBContainer.start();

		return mongoDBContainer;
	}

	private OqmCoreApiWebServiceContainer newCoreApiContainer(
		CoreApiLibBuildTimeConfig config,
		Map<String, String> mongoConnectionInfo,
		Map<String, String> kafkaConnectionInfo
	) {
		log.info("Building new OQM Core API dev container");
		OqmCoreApiWebServiceContainer
			container =
			new OqmCoreApiWebServiceContainer(config.devservices(), mongoConnectionInfo, kafkaConnectionInfo)
			//				.withAccessToHost(true)
			//				.withNetwork(Network.SHARED)
			;

		//configure network

		//TODO:: #1287 these lines are related to not host netowrking
		ConfigureUtil.configureSharedNetwork(container, "oqm-core-api");
		container.withExposedPorts(8080);
		container.withAccessToHost(true);

		//set _something_ to satisfy startup for testing
		//		container.withEnv(
		//			"smallrye.jwt.verify.key.location",
		//			String.format(
		//				"http://%s/realms/%s/protocol/openid-connect/certs",
		//				"foo:8080",
		//				"oqm-app"
		//			)
		//		);

		//		container.start();

		return container;
	}


	@BuildStep(onlyIfNot = IsNormal.class, onlyIf = DevServicesConfig.Enabled.class)
	public List<DevServicesResultBuildItem> createContainer(LaunchModeBuildItem launchMode, CoreApiLibBuildTimeConfig config, CuratedApplicationShutdownBuildItem closeBuildItem) {
		log.info("Setting up OQM Core API related dev services.");

		//TODO:: handle needing to restart services?
		List<DevServicesResultBuildItem> output = new ArrayList<>();
		Map<String, String> mongoConnectionInfo = new HashMap<>();
		Map<String, String> kafkaConnectionInfo = new HashMap<>();
		{//mongodb

			DevServicesResultBuildItem mongoDevService = DEVSERVICES.get("mongodb");

			if (mongoDevService == null) {
				MongoDBContainer mongoDBContainer = newMongoDbContainer();

				log.info("MongoDB dev service network aliases: " + mongoDBContainer.getNetworkAliases());

				Map<String, String> props = Map.of(
					"host",
					mongoDBContainer.getNetworkAliases().get(0),

					"port",
					"27017" //String.valueOf(mongoDBContainer.getMappedPort(27017))//TODO:: #1287
				);

				log.info("MongoDB dev service properties: {}" + props);

				mongoDevService = DevServicesResultBuildItem.discovered()
									  .feature(FEATURE+"-mongo")
									  .containerId(mongoDBContainer.getContainerId())
									  .config(props)
									  .build();

				DEVSERVICES.put("mongodb", mongoDevService);
			}

			mongoConnectionInfo.put(
				"quarkus.mongodb.connection-string",
				"mongodb://" + mongoDevService.getConfig().get("host") + ":" + mongoDevService.getConfig().get("port")
			);

			output.add(mongoDevService);
		}

		//TODO:: move this to other means
		if (config.devservices().kafka().enabled()) {//connect to existent
			log.info("Connecting to existing kafka dev service.");
			kafkaConnectionInfo.putAll(Map.of(
				"mp.messaging.outgoing.events-outgoing.enabled", "true",
				"mp.messaging.outgoing.events-outgoing.bootstrap.servers", String.format("OUTSIDE://%s:%d", KAFKA_DEVSERVICE_HOSTNAME, config.devservices().kafka().port())
			));
		} else {
			log.info("NOT Connecting to existing kafka dev service.");
			kafkaConnectionInfo.putAll(Map.of(
				"mp.messaging.outgoing.events-outgoing.enabled", "false"
			));
		}

		{//Core API
			DevServicesResultBuildItem coreApiDevService = DEVSERVICES.get("coreApi");

			if (coreApiDevService == null) {
				coreApiDevService = DevServicesResultBuildItem.owned()
										.feature(FEATURE)
										.startable(()->{
											return this.newCoreApiContainer(config, mongoConnectionInfo, kafkaConnectionInfo);
										})
										.dependsOnConfig(
											"keycloak.auth-server-internal-url",
											(container, host)->{
												container.withEnv(
													"smallrye.jwt.verify.key.location",
													String.format(
														"%s/realms/%s/protocol/openid-connect/certs",
														host,
														config.devservices().keycloak().realm()
													)
												);
											},
											true
										)
										.configProvider(Map.of(
											//main config value
											Constants.CONFIG_ROOT_NAME + ".baseUri", (container)->"http://" + container.getHost() + ":" + container.getPort(),
											//ensuring rest client is set properly
											"quarkus.rest-client.\"" + Constants.CORE_API_CLIENT_NAME + "\".url", (container)->"${" + Constants.CONFIG_ROOT_NAME + ".baseUri}"
										))
										.build()
				;

				DEVSERVICES.put("coreApi", coreApiDevService);
				log.info("Built new core api devservice / " + Constants.CONFIG_ROOT_NAME + ".baseUri");
			}

			output.add(coreApiDevService);
		}

		if (firstSetup) {
			firstSetup = false;
			//TODO:: determine if necessary
			//			closeBuildItem.addCloseTask(
			//				()->{
			//					while (!DEVSERVICES.isEmpty()) {
			//						String curDevservice = DEVSERVICES.keySet().stream().findFirst().get();
			//						try (
			//							DevServicesResultBuildItem cur = DEVSERVICES.remove(curDevservice)
			//						) {
			//							log.info("Closing devservice " + curDevservice + ": " + cur);
			//						} catch(IOException e) {
			//							log.error("Failed to close devservice: " + curDevservice.toString(), e);
			//						}
			//					}
			//
			//					firstSetup = true;
			//				}, true
			//			);
		}

		return output;
	}

	@BuildStep(onlyIf = IsLocalDevelopment.class)
	void setupDevUiCard(BuildProducer<CardPageBuildItem> cardsProducer, CoreApiLibBuildTimeConfig config) {

		CardPageBuildItem cardPageBuildItem = new CardPageBuildItem();
		cardPageBuildItem.setLogo("oqm-icon.svg", "oqm-icon.svg");

		//show oqm core api ui
		cardPageBuildItem.addPage(
			Page.externalPageBuilder("OQM Core API UI")
				.url("http://localhost:" + config.devservices().port())
				.doNotEmbed()//needed as embedded fails due to CORS
		);

		//page for managing core api data
		cardPageBuildItem.addPage(
			Page.webComponentPageBuilder()
				.title("DB Management")
				.icon("font-awesome-solid:database")
				.componentLink("qwc-oqm-core-api-lib-db-management.js")
		);

		cardsProducer.produce(cardPageBuildItem);
	}

	@BuildStep(onlyIf = IsLocalDevelopment.class)
	JsonRPCProvidersBuildItem createJsonRPCService() {
		return new JsonRPCProvidersBuildItem(tech.ebp.oqm.lib.core.api.quarkus.runtime.dev.CoreApiDevDbManagementService.class);
	}
}
