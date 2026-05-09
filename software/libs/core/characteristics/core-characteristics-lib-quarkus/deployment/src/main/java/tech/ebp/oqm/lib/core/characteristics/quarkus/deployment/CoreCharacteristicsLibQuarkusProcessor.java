package tech.ebp.oqm.lib.core.characteristics.quarkus.deployment;

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
import tech.ebp.oqm.lib.core.characteristics.quarkus.deployment.config.CoreCharacteristicsLibBuildTimeConfig;
import tech.ebp.oqm.lib.core.characteristics.quarkus.deployment.testContainers.OqmCoreCharacteristicsWebServiceContainer;
import tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.Constants;
import tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.config.OqmCoreCharacteristicsConfig;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Processes runtime features like configs, health checks, and devservices
 */
class CoreCharacteristicsLibQuarkusProcessor {
	
	private static final Logger log = Logger.getLogger(CoreCharacteristicsLibQuarkusProcessor.class);
	
	private static final String FEATURE = "core-characteristics-lib-quarkus";
	private static final String HOST = "localhost";
	
	private static volatile boolean firstSetup = true;
	
	private static volatile Map<String, DevServicesResultBuildItem.RunningDevService> DEVSERVICES = new HashMap<>();
	
	
	@BuildStep
	FeatureBuildItem feature() {
		return new FeatureBuildItem(FEATURE);
	}
	
	@BuildStep
	List<RunTimeConfigurationDefaultBuildItem> addRestConfiguration() {
		return List.of(
			new RunTimeConfigurationDefaultBuildItem("quarkus.rest-client.\"" + Constants.REST_CLIENT_NAME + "\".url", "${" + Constants.CONFIG_ROOT_NAME + ".baseUri}")
		);
	}
	
	@BuildStep
	HealthBuildItem addHealthCheck(CoreCharacteristicsLibBuildTimeConfig buildTimeConfig) {
		return new HealthBuildItem("tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.health.CoreCharacteristicsHealthCheck", buildTimeConfig.health().enabled());
	}
	
	
	private OqmCoreCharacteristicsWebServiceContainer newCoreCharacteristicsContainer(
		CoreCharacteristicsLibBuildTimeConfig config
	) {
		log.info("Starting new OQM Core Characteristics dev container");
		OqmCoreCharacteristicsWebServiceContainer
			container =
			new OqmCoreCharacteristicsWebServiceContainer(config);
		
		container.start();
		
		return container;
	}
	
	
	@BuildStep(onlyIfNot = IsNormal.class, onlyIf = DevServicesConfig.Enabled.class)
	public List<DevServicesResultBuildItem> createContainer(
		LaunchModeBuildItem launchMode,
		CoreCharacteristicsLibBuildTimeConfig config,
		CuratedApplicationShutdownBuildItem closeBuildItem
	) {
		log.info("Setting up OQM Core API related dev services.");
		//TODO:: handle needing to restart services?
		List<DevServicesResultBuildItem> output = new ArrayList<>();
		{//Core Characteristics
			DevServicesResultBuildItem.RunningDevService coreApiDevService = DEVSERVICES.get("coreCharacteristics");
			
			if (coreApiDevService == null) {
				OqmCoreCharacteristicsWebServiceContainer container = this.newCoreCharacteristicsContainer(config);
				
				Map<String, String> props = new HashMap<>();
				props.put(Constants.CONFIG_ROOT_NAME + ".baseUri", "http://" + container.getHost() + ":" + container.getMappedPort(80));
				props.put("quarkus.rest-client.\"" + Constants.REST_CLIENT_NAME + "\".url", "${" + Constants.CONFIG_ROOT_NAME + ".baseUri}");
				
				coreApiDevService = new DevServicesResultBuildItem.RunningDevService(FEATURE, container.getContainerId(), container::close, props);
				DEVSERVICES.put("coreCharacteristics", coreApiDevService);
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
