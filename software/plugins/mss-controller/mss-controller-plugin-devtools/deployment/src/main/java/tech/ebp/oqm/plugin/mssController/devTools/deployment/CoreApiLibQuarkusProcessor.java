package tech.ebp.oqm.plugin.mssController.devTools.deployment;

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
import org.testcontainers.utility.MountableFile;
import tech.ebp.oqm.plugin.mssController.devTools.runtime.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class CoreApiLibQuarkusProcessor {
	
	private static final String FEATURE = "mss-controller-devtooling";
	
	@BuildStep
	FeatureBuildItem feature() {
		return new FeatureBuildItem(FEATURE);
	}
	
//	@BuildStep
//	List<RunTimeConfigurationDefaultBuildItem> addRestConfiguration() {
//		return List.of(
//			new RunTimeConfigurationDefaultBuildItem("quarkus.rest-client."+Constants.CORE_API_CLIENT_NAME+".url", "${quarkus." + Constants.CONFIG_ROOT_NAME + ".coreApiBaseUri}"),
//			new RunTimeConfigurationDefaultBuildItem("quarkus.rest-client."+Constants.CORE_API_CLIENT_OIDC_NAME+".url", "${quarkus.oidc.auth-server-url:}")
//		);
//	}

	@BuildStep(onlyIfNot = IsNormal.class, onlyIf = GlobalDevServicesConfig.Enabled.class)
	public List<DevServicesResultBuildItem> createContainer(
		LaunchModeBuildItem launchMode,
		CoreApiLibBuildTimeConfig config
	) {
		List<DevServicesResultBuildItem> output = new ArrayList<>();
		Map<String, String> mongoConnectionInfo = new HashMap<>();
		return List.of();
	}
}
