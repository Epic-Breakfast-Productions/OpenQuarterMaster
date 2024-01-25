package tech.ebp.oqm.lib.core.api.quarkus.deployment;

import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.dev.devservices.GlobalDevServicesConfig;
import io.quarkus.smallrye.health.deployment.spi.HealthBuildItem;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Map;

class CoreApiLibQuarkusProcessor {
	
	private static final String FEATURE = "core-api-lib-quarkus";
	
	@BuildStep
	FeatureBuildItem feature() {
		return new FeatureBuildItem(FEATURE);
	}
	
	@BuildStep(onlyIfNot = IsNormal.class, onlyIf = GlobalDevServicesConfig.Enabled.class)
	public List<DevServicesResultBuildItem> createContainer(LaunchModeBuildItem launchMode) {
		
		DockerImageName dockerImageName = DockerImageName.parse("ebprod/oqm-core-api:1.0.0");
		
		// You might want to use Quarkus config here to customise the container
		OtherWebServiceContainer container = new OtherWebServiceContainer(dockerImageName)
												 .withEnv("whatever", "true")
												 .withEnv("otherenv", "something");
		
		container.start();
		
		Map<String, String> props = Map.of(
			"mynew.url",
			"https://" + container.getHost() + ":" + container.getPort()
		);
		
		
		return List.of(new DevServicesResultBuildItem.RunningDevService(
				FEATURE,
				container.getContainerId(),
				container::close,
				props
			)
						   .toBuildItem()
		);
	}
	
	private static class OtherWebServiceContainer extends GenericContainer<OtherWebServiceContainer> {
		
		static final int PORT = 25565;
		
		public OtherWebServiceContainer(DockerImageName image) {
			super(image);
		}
		
		@Override
		protected void configure() {
			withNetwork(Network.SHARED);
			addExposedPorts(PORT);
			// Tell the dev service how to know the container is ready
			waitingFor(Wait.forLogMessage(".*Listening on http://0.0.0.0:9292.*", 1));
		}
		
		public Integer getPort() {
			return this.getMappedPort(PORT);
		}
	}
	
	@BuildStep
	HealthBuildItem addHealthCheck(CoreApiLibBuildTimeConfig buildTimeConfig) {
		return new HealthBuildItem(
			"tech.ebp.oqm.lib.core.api.quarkus.runtime.CoreApiHealthCheck",
			buildTimeConfig.healthEnabled
		);
	}
}
