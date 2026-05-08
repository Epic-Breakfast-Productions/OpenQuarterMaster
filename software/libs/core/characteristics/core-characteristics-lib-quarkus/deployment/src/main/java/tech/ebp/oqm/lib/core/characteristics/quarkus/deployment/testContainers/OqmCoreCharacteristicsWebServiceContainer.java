package tech.ebp.oqm.lib.core.characteristics.quarkus.deployment.testContainers;

import com.github.dockerjava.api.model.HostConfig;
import io.quarkus.devservices.common.ConfigureUtil;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import tech.ebp.oqm.lib.core.characteristics.quarkus.deployment.config.CoreCharacteristicsLibBuildTimeConfig;

import java.util.Map;

/**
 * Container for the Open QuarterMaster Core API web service.
 */
public class OqmCoreCharacteristicsWebServiceContainer extends GenericContainer<OqmCoreCharacteristicsWebServiceContainer> {
	
	private final CoreCharacteristicsLibBuildTimeConfig.DevserviceConfig devserviceConfig;
	
	/**
	 * Initializes the container
	 * @param devserviceConfig The devservice config to use to set this container up.
	 */
	public OqmCoreCharacteristicsWebServiceContainer(
		CoreCharacteristicsLibBuildTimeConfig.DevserviceConfig devserviceConfig
	) {
		super(devserviceConfig.image().toTestContainerImageName());
		this.devserviceConfig = devserviceConfig;
	}
	
	@Override
	protected void configure() {
		//configure network
		ConfigureUtil.configureSharedNetwork(this, "oqm-core-characteristics");
		
		// Tell the dev service how to know the container is ready. All 3 is likely overkill, but eh
		this.waitingFor(Wait.forHealthcheck());
		this.addExposedPort(80);
	}
}
