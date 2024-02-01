package tech.ebp.oqm.lib.core.api.quarkus.deployment;

import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.builditem.RunTimeConfigurationDefaultBuildItem;
import io.quarkus.deployment.dev.devservices.GlobalDevServicesConfig;
import io.quarkus.smallrye.health.deployment.spi.HealthBuildItem;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class CoreApiLibQuarkusProcessor {
	
	private static final String FEATURE = "core-api-lib-quarkus";
	private static final String MONGODB_DEVSERVICE_HOSTNAME = "mongodbserver";
	
	@BuildStep
	FeatureBuildItem feature() {
		return new FeatureBuildItem(FEATURE);
	}
	
	@BuildStep
	List<RunTimeConfigurationDefaultBuildItem> addRestConfiguration() {
		return List.of(
			new RunTimeConfigurationDefaultBuildItem("quarkus.rest-client.oqmCoreApi.url", "${quarkus." + Constants.CONFIG_ROOT_NAME + ".coreApiBaseUri}")
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
	public List<DevServicesResultBuildItem> createContainer(LaunchModeBuildItem launchMode) {
		List<DevServicesResultBuildItem> output = new ArrayList<>();
		Map<String, String> mongoConnectionInfo = new HashMap<>();
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
		{//Base Station
			DockerImageName dockerImageName = DockerImageName.parse("ebprod/oqm-core-api:1.0.0");
			// You might want to use Quarkus config here to customise the container
			OqmCoreApiWebServiceContainer container = new OqmCoreApiWebServiceContainer(dockerImageName)
														  .withAccessToHost(true)
														  .withEnv(mongoConnectionInfo)
														  .withNetwork(Network.SHARED);
			;
			
			container.start();
			
			Map<String, String> props = new HashMap<>();
			props.put("quarkus." + Constants.CONFIG_ROOT_NAME + ".coreApiBaseUri", "http://" + container.getHost() + ":" + container.getPort());
			props.put("quarkus.rest-client.oqmCoreApi.url", "${quarkus." + Constants.CONFIG_ROOT_NAME + ".coreApiBaseUri}");
			
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
