package tech.ebp.oqm.lib.core.api.quarkus.deployment;

import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.builditem.RunTimeConfigurationDefaultBuildItem;
import io.quarkus.deployment.dev.devservices.GlobalDevServicesConfig;
import io.quarkus.smallrye.health.deployment.spi.HealthBuildItem;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.redpanda.RedpandaContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.Constants;

import java.io.File;
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
			new RunTimeConfigurationDefaultBuildItem("quarkus.rest-client." + Constants.CORE_API_CLIENT_NAME + ".url", "${quarkus." + Constants.CONFIG_ROOT_NAME + ".coreApiBaseUri}"),
			new RunTimeConfigurationDefaultBuildItem("quarkus.rest-client." + Constants.CORE_API_CLIENT_OIDC_NAME + ".url", "${quarkus.oidc.auth-server-url:}")
		);
	}

	@BuildStep
	HealthBuildItem addHealthCheck(CoreApiLibBuildTimeConfig buildTimeConfig) {
		return new HealthBuildItem(
			"tech.ebp.oqm.lib.core.api.quarkus.runtime.CoreApiHealthCheck",
			buildTimeConfig.healthEnabled
		);
	}

	@BuildStep(onlyIfNot = IsNormal.class, onlyIf = GlobalDevServicesConfig.Enabled.class)
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
		if (config.devservice.enableKafka) {
			//TODO:: allow connection from core api devservice, and the using app. Update hostname, replace in bootstrap?
			RedpandaContainer kafka = new RedpandaContainer(DockerImageName.parse("docker.redpanda.com/redpandadata/redpanda:v23.1.2"))
				.withNetwork(Network.SHARED)
				.withAccessToHost(true)
				.withNetworkAliases(KAFKA_DEVSERVICE_HOSTNAME)
//				.withListener(() -> "external://"+KAFKA_DEVSERVICE_HOSTNAME + ":9092")
				.withListener(() -> KAFKA_DEVSERVICE_HOSTNAME + ":19092")
//				.withListener(() -> KAFKA_DEVSERVICE_HOSTNAME + ":9093")

				// { {external:{host: localhost, port: 32884}}, {internal:{host: 127.0.0.1, port: 9093}}}
//				.withEnv("REDPANDA_KAFKA_ADVERTISED_LISTENERS", String.format(
//						"internal://%s:9093,external://%s:9092",
//						KAFKA_DEVSERVICE_HOSTNAME,
//						KAFKA_DEVSERVICE_HOSTNAME
//					))
				;
//			KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))
//				.withKraft()
//				.withNetwork(Network.SHARED)
//				.withAccessToHost(true)
//				.withNetworkAliases(KAFKA_DEVSERVICE_HOSTNAME)
//				.withListener(() -> KAFKA_DEVSERVICE_HOSTNAME + ":9092")
//				;
			kafka.start();

			//TODO:: fix; RP advertises localhost for listeners despite supplying the correct hostname as config
			String bootstrapServers = kafka.getBootstrapServers();
			kafkaConnectionInfo.putAll(Map.of(
				"quarkus.reactive-messaging.health.enabled", "true",
				"mp.messaging.outgoing.events-outgoing.bootstrap.servers", String.format("PLAINTEXT://%s:%d", KAFKA_DEVSERVICE_HOSTNAME, 19092),
				"devservice.kafka.bootstrapServers", kafka.getBootstrapServers(),
				"mp.messaging.outgoing.events-outgoing.connector", "smallrye-kafka",
				"mp.messaging.outgoing.events-outgoing.broadcast", "true",
				"mp.messaging.outgoing.events-outgoing.value.serializer", "io.quarkus.kafka.client.serialization.ObjectMapperSerializer"
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
		{//Base Station
			DockerImageName dockerImageName = DockerImageName.parse("ebprod/oqm-core-api:" + config.devservice.coreApiVersion);
			// You might want to use Quarkus config here to customise the container
			OqmCoreApiWebServiceContainer container = new OqmCoreApiWebServiceContainer(dockerImageName)
				.withAccessToHost(true)
				.withEnv(mongoConnectionInfo)
				.withEnv(kafkaConnectionInfo)
				.withNetwork(Network.SHARED);
			;

			if (
				config.devservice.certKeyPath.isPresent() ||
					config.devservice.certPath.isPresent()
			) {
				if (!(config.devservice.certKeyPath.isPresent() &&
					config.devservice.certPath.isPresent())) {
					throw new RuntimeException("Must specify both cert and key for core api devservice.");
				}

				File cert = config.devservice.certPath.get();
				File key = config.devservice.certKeyPath.get();

				container.withCopyFileToContainer(
					MountableFile.forHostPath(cert.getPath()),
					"/tmp/systemCert.pem"
				);
				container.withCopyFileToContainer(
					MountableFile.forHostPath(key.getPath()),
					"/tmp/systemCertKey.pem"
				);
				container.withEnv("mp.jwt.verify.publickey.location", "/tmp/systemCert.pem");

			}

			container.start();

			Map<String, String> props = new HashMap<>();
			props.put("quarkus." + Constants.CONFIG_ROOT_NAME + ".coreApiBaseUri", "http://" + container.getHost() + ":" + container.getPort());
			props.put("quarkus.rest-client.oqmCoreApi.url", "${quarkus." + Constants.CONFIG_ROOT_NAME + ".coreApiBaseUri}");

			if (!kafkaConnectionInfo.isEmpty()) {
				props.put("devservice.kafka.bootstrapServers", kafkaConnectionInfo.get("devservice.kafka.bootstrapServers"));
			}

			output.add(new DevServicesResultBuildItem.RunningDevService(
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
