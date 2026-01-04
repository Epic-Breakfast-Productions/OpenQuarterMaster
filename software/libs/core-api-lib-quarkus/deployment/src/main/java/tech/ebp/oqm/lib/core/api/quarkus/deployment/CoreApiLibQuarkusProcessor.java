package tech.ebp.oqm.lib.core.api.quarkus.deployment;

import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CuratedApplicationShutdownBuildItem;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.builditem.RunTimeConfigurationDefaultBuildItem;
import io.quarkus.deployment.dev.devservices.DevServicesConfig;
import io.quarkus.smallrye.health.deployment.spi.HealthBuildItem;
import org.jboss.logging.Logger;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.redpanda.RedpandaContainer;
import org.testcontainers.utility.DockerImageName;
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
	private static final String MONGODB_DEVSERVICE_HOSTNAME = "oqm-dev-mongodb-server";
	private static final String KAFKA_DEVSERVICE_HOSTNAME = "oqm-dev-kafka-server";
	
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
		DockerImageName mongoImageName = DockerImageName.parse("mongo:7");
		
		MongoDBContainer mongoDBContainer = new MongoDBContainer(mongoImageName);
		mongoDBContainer.addExposedPorts();
		mongoDBContainer.withNetwork(Network.SHARED);
		mongoDBContainer.withNetworkAliases(MONGODB_DEVSERVICE_HOSTNAME);
		mongoDBContainer.start();
		
		return mongoDBContainer;
	}
	
	private RedpandaContainer newKafkaContainer() {
		RedpandaContainer
			kafka =
			new RedpandaContainer(DockerImageName.parse("docker.redpanda.com/redpandadata/redpanda:v23.1.2")).withNetwork(Network.SHARED)
				.withAccessToHost(true)
				.withNetworkAliases(KAFKA_DEVSERVICE_HOSTNAME)
				.withListener(()->KAFKA_DEVSERVICE_HOSTNAME + ":19092");
		kafka.start();
		
		return kafka;
	}
	
	private OqmCoreApiWebServiceContainer newCoreApiContainer(CoreApiLibBuildTimeConfig config, Map<String, String> mongoConnectionInfo, Map<String, String> kafkaConnectionInfo) {
		DockerImageName dockerImageName = DockerImageName.parse("docker.io/ebprod/oqm-core-api:" + config.devservice().coreApiVersion());
		
		OqmCoreApiWebServiceContainer
			container =
			new OqmCoreApiWebServiceContainer(dockerImageName, config.devservice()).withAccessToHost(true)
				.withEnv(mongoConnectionInfo)
				.withEnv(kafkaConnectionInfo)
				.withNetwork(Network.SHARED);
		
		container.withEnv(
			"smallrye.jwt.verify.key.location",
			String.format(
				"http://host.testcontainers.internal:%s/realms/%s/protocol/openid-connect/certs",
				config.devservice().keycloak().port(),
				config.devservice().keycloak().realm()
			)
		);
		
		container.start();
		
		return container;
	}
	
	
	@BuildStep(onlyIfNot = IsNormal.class, onlyIf = DevServicesConfig.Enabled.class)
	public List<DevServicesResultBuildItem> createContainer(LaunchModeBuildItem launchMode, CoreApiLibBuildTimeConfig config, CuratedApplicationShutdownBuildItem closeBuildItem) {
		//TODO:: handle needing to restart services?
		List<DevServicesResultBuildItem> output = new ArrayList<>();
		Map<String, String> mongoConnectionInfo = new HashMap<>();
		Map<String, String> kafkaConnectionInfo = new HashMap<>();
		{//mongodb
			mongoConnectionInfo.put("quarkus.mongodb.connection-string", "mongodb://" + MONGODB_DEVSERVICE_HOSTNAME + ":27017");
			
			DevServicesResultBuildItem.RunningDevService mongoDevService = DEVSERVICES.get("mongodb");
			
			if (mongoDevService == null) {
				MongoDBContainer mongoDBContainer = newMongoDbContainer();
				mongoDevService = new DevServicesResultBuildItem.RunningDevService(FEATURE, mongoDBContainer.getContainerId(), mongoDBContainer::close, Map.of());
				
				DEVSERVICES.put("mongodb", mongoDevService);
			}
			
			output.add(mongoDevService.toBuildItem());
		}
		{
			DevServicesResultBuildItem.RunningDevService kafkaDevService = DEVSERVICES.get("kafka");
			
			if (kafkaDevService == null) {
				RedpandaContainer kafka = this.newKafkaContainer();
				
				kafkaDevService = new DevServicesResultBuildItem.RunningDevService(FEATURE, kafka.getBootstrapServers(), kafka.getContainerId(), kafka::close, Map.of());
				DEVSERVICES.put("kafka", kafkaDevService);
			}
			
			kafkaConnectionInfo.putAll(Map.of(
				"quarkus.reactive-messaging.health.enabled",
				"true",
				"mp.messaging.outgoing.events-outgoing.bootstrap.servers",
				String.format("PLAINTEXT://%s:%d", KAFKA_DEVSERVICE_HOSTNAME, 19092),
				"devservice.kafka.bootstrapServers",
				kafkaDevService.getDescription()
			));
			
			output.add(kafkaDevService.toBuildItem());
		}
		{//Core API
			DevServicesResultBuildItem.RunningDevService coreApiDevService = DEVSERVICES.get("coreApi");
			
			if (coreApiDevService == null) {
				OqmCoreApiWebServiceContainer container = this.newCoreApiContainer(config, mongoConnectionInfo, kafkaConnectionInfo);
				Map<String, String> props = new HashMap<>();
				props.put(Constants.CONFIG_ROOT_NAME + ".baseUri", "http://" + container.getHost() + ":" + container.getPort());
				props.put("quarkus.rest-client.\"" + Constants.CORE_API_CLIENT_NAME + "\".url", "${" + Constants.CONFIG_ROOT_NAME + ".baseUri}");
				
				if (!kafkaConnectionInfo.isEmpty()) {
					props.put("devservice.kafka.bootstrapServers", kafkaConnectionInfo.get("devservice.kafka.bootstrapServers"));
				}
				
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
}
