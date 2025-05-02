package tech.ebp.oqm.lib.core.api.quarkus.deployment;

import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.builditem.RunTimeConfigurationDefaultBuildItem;
import io.quarkus.deployment.dev.devservices.DevServicesConfig;
import io.quarkus.smallrye.health.deployment.spi.HealthBuildItem;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.redpanda.RedpandaContainer;
import org.testcontainers.utility.DockerImageName;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class CoreApiLibQuarkusProcessor {
	
	private static final String FEATURE = "core-api-lib-quarkus";
	private static final String MONGODB_DEVSERVICE_HOSTNAME = "oqm-dev-mongodb-server";
	private static final String KAFKA_DEVSERVICE_HOSTNAME = "oqm-dev-kafka-server";
	
	@BuildStep
	FeatureBuildItem feature() {
		return new FeatureBuildItem(FEATURE);
	}
	
	@BuildStep
	List<RunTimeConfigurationDefaultBuildItem> addRestConfiguration() {
		return List.of(
			new RunTimeConfigurationDefaultBuildItem(
				"quarkus.rest-client.\"" + Constants.CORE_API_CLIENT_NAME + "\".url",
				"${" + Constants.CONFIG_ROOT_NAME + ".baseUri}"
			),
			new RunTimeConfigurationDefaultBuildItem("quarkus.rest-client.\"" + Constants.CORE_API_CLIENT_OIDC_NAME + "\".url", "${quarkus.oidc.auth-server-url:}")
		);
	}
	
	@BuildStep
	HealthBuildItem addHealthCheck(CoreApiLibBuildTimeConfig buildTimeConfig) {
		return new HealthBuildItem(
			"tech.ebp.oqm.lib.core.api.quarkus.runtime.CoreApiHealthCheck",
			buildTimeConfig.health().enabled()
		);
	}
	
	@BuildStep(onlyIfNot = IsNormal.class, onlyIf = DevServicesConfig.Enabled.class)
	public List<DevServicesResultBuildItem> createContainer(
		LaunchModeBuildItem launchMode,
		CoreApiLibBuildTimeConfig config
	) {
		List<DevServicesResultBuildItem> output = new ArrayList<>();
		Map<String, String> mongoConnectionInfo = new HashMap<>();
		Map<String, String> kafkaConnectionInfo = new HashMap<>();
		{//mongodb
			DockerImageName mongoImageName = DockerImageName.parse("mongo:7");
			
			MongoDBContainer mongoDBContainer = new MongoDBContainer(mongoImageName);
			mongoDBContainer.addExposedPorts();
			mongoDBContainer.withNetwork(Network.SHARED);
			mongoDBContainer.withNetworkAliases(MONGODB_DEVSERVICE_HOSTNAME);
			mongoDBContainer.start();
			
			mongoConnectionInfo.put("quarkus.mongodb.connection-string", "mongodb://" + MONGODB_DEVSERVICE_HOSTNAME + ":27017");
			
			output.add(new DevServicesResultBuildItem.RunningDevService(
					FEATURE,
					mongoDBContainer.getContainerId(),
					mongoDBContainer::close,
					Map.of()
				)
						   .toBuildItem()
			);
		}
		{
			RedpandaContainer kafka = new RedpandaContainer(DockerImageName.parse("docker.redpanda.com/redpandadata/redpanda:v23.1.2"))
										  .withNetwork(Network.SHARED)
										  .withAccessToHost(true)
										  .withNetworkAliases(KAFKA_DEVSERVICE_HOSTNAME)
										  .withListener(()->KAFKA_DEVSERVICE_HOSTNAME + ":19092");
			kafka.start();
			
			kafkaConnectionInfo.putAll(Map.of(
				"quarkus.reactive-messaging.health.enabled", "true",
				"mp.messaging.outgoing.events-outgoing.bootstrap.servers", String.format("PLAINTEXT://%s:%d", KAFKA_DEVSERVICE_HOSTNAME, 19092),
				"devservice.kafka.bootstrapServers", kafka.getBootstrapServers()
			));
			
			output.add(
				new DevServicesResultBuildItem.RunningDevService(
					FEATURE,
					kafka.getContainerId(),
					kafka::close,
					Map.of()
				).toBuildItem()
			);
		}
		{//Core API
			DockerImageName dockerImageName = DockerImageName.parse("docker.io/ebprod/oqm-core-api:" + config.devservice().coreApiVersion());
			
			OqmCoreApiWebServiceContainer container = new OqmCoreApiWebServiceContainer(
				dockerImageName,
				config.devservice()
			)
														  .withAccessToHost(true)
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
			
			Map<String, String> props = new HashMap<>();
			props.put(Constants.CONFIG_ROOT_NAME + ".baseUri", "http://" + container.getHost() + ":" + container.getPort());
			props.put("quarkus.rest-client.\"" + Constants.CORE_API_CLIENT_NAME + "\".url", "${" + Constants.CONFIG_ROOT_NAME + ".baseUri}");
			
			if (!kafkaConnectionInfo.isEmpty()) {
				props.put("devservice.kafka.bootstrapServers", kafkaConnectionInfo.get("devservice.kafka.bootstrapServers"));
			}
			
			output.add(
				new DevServicesResultBuildItem.RunningDevService(
					FEATURE,
					container.getContainerId(),
					container::close,
					props
				)
					.toBuildItem()
			);
		}
		
		return output;
	}
}
