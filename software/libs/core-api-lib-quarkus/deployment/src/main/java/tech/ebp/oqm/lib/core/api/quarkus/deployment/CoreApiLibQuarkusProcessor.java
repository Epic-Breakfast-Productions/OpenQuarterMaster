package tech.ebp.oqm.lib.core.api.quarkus.deployment;

import io.quarkus.deployment.IsLocalDevelopment;
import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CuratedApplicationShutdownBuildItem;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.builditem.RunTimeConfigurationDefaultBuildItem;
import io.quarkus.deployment.dev.devservices.DevServicesConfig;
import io.quarkus.devui.spi.JsonRPCProvidersBuildItem;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.Page;
import io.quarkus.smallrye.health.deployment.spi.HealthBuildItem;
import org.jboss.logging.Logger;
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
	private static final String MONGODB_DEVSERVICE_HOSTNAME = "localhost";
	private static final String HOST = "localhost";
	private static final String KEYCLOAK_DEVSERVICE_HOSTNAME = HOST;
	private static final String KAFKA_DEVSERVICE_HOSTNAME = HOST;
	
	private static volatile boolean firstSetup = true;
	
	private static volatile Map<String, DevServicesResultBuildItem.RunningDevService> DEVSERVICES = new HashMap<>();
	
	
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
		mongoDBContainer.addExposedPorts();
		mongoDBContainer.withNetwork(Network.SHARED);
//		mongoDBContainer.withNetworkAliases(MONGODB_DEVSERVICE_HOSTNAME);
		mongoDBContainer.start();
		
		return mongoDBContainer;
	}
	
	private OqmCoreApiWebServiceContainer newCoreApiContainer(
		CoreApiLibBuildTimeConfig config,
		Map<String, String> mongoConnectionInfo,
		Map<String, String> kafkaConnectionInfo
	) {
		log.info("Starting new OQM Core API dev container");
		OqmCoreApiWebServiceContainer
			container =
			new OqmCoreApiWebServiceContainer(config.devservices(), mongoConnectionInfo, kafkaConnectionInfo)
			//				.withAccessToHost(true)
			//				.withNetwork(Network.SHARED)
			;
		
		container.withEnv(
			"smallrye.jwt.verify.key.location",
			String.format(
				"http://%s:%s/realms/%s/protocol/openid-connect/certs",
				KEYCLOAK_DEVSERVICE_HOSTNAME,
				config.devservices().keycloak().port(),
				config.devservices().keycloak().realm()
			)
		);
		
		container.start();
		
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
			
			DevServicesResultBuildItem.RunningDevService mongoDevService = DEVSERVICES.get("mongodb");
			
			if (mongoDevService == null) {
				MongoDBContainer mongoDBContainer = newMongoDbContainer();
				mongoDevService = new DevServicesResultBuildItem.RunningDevService(
					FEATURE,
					mongoDBContainer.getContainerId(),
					mongoDBContainer::close,
					Map.of(
						"port",
						String.valueOf(mongoDBContainer.getMappedPort(27017))
					)
				);
				
				
				DEVSERVICES.put("mongodb", mongoDevService);
			}
			
			mongoConnectionInfo.put(
				"quarkus.mongodb.connection-string",
				"mongodb://" + MONGODB_DEVSERVICE_HOSTNAME + ":" + mongoDevService.getConfig().get("port")
			);
			
			output.add(mongoDevService.toBuildItem());
		}
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
			DevServicesResultBuildItem.RunningDevService coreApiDevService = DEVSERVICES.get("coreApi");
			
			if (coreApiDevService == null) {
				OqmCoreApiWebServiceContainer container = this.newCoreApiContainer(config, mongoConnectionInfo, kafkaConnectionInfo);
				
				Map<String, String> props = new HashMap<>();
				props.put(Constants.CONFIG_ROOT_NAME + ".baseUri", "http://" + container.getHost() + ":" + container.getPort());
				props.put("quarkus.rest-client.\"" + Constants.CORE_API_CLIENT_NAME + "\".url", "${" + Constants.CONFIG_ROOT_NAME + ".baseUri}");
				
				coreApiDevService = new DevServicesResultBuildItem.RunningDevService(FEATURE, container.getContainerId(), container::close, props);
				DEVSERVICES.put("coreApi", coreApiDevService);
			}
			
			output.add(coreApiDevService.toBuildItem());
		}
		
		if (firstSetup) {
			firstSetup = false;
			closeBuildItem.addCloseTask(
				()->{
					while (!DEVSERVICES.isEmpty()) {
						String curDevservice = DEVSERVICES.keySet().stream().findFirst().get();
						try (
							Closeable cur = DEVSERVICES.remove(curDevservice)
						) {
							log.info("Closing devservice " + curDevservice + ": " + cur);
						} catch(IOException e) {
							log.error("Failed to close devservice: " + curDevservice.toString(), e);
						}
					}
					
					firstSetup = true;
				}, true
			);
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
