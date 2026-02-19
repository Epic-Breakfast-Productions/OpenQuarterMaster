package tech.ebp.oqm.lib.core.api.quarkus.deployment.testContainers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import tech.ebp.oqm.lib.core.api.quarkus.deployment.config.CoreApiLibBuildTimeConfig;

public class OqmCoreApiWebServiceContainer extends GenericContainer<OqmCoreApiWebServiceContainer> {
	
	public static final int PORT = 8123;
	
	private final CoreApiLibBuildTimeConfig.DevserviceConfig devserviceConfig;
	
	public OqmCoreApiWebServiceContainer(DockerImageName image, CoreApiLibBuildTimeConfig.DevserviceConfig devserviceConfig) {
		super(image);
		this.devserviceConfig = devserviceConfig;
	}
	
	@Override
	protected void configure() {
		withNetwork(Network.SHARED);
		
		Testcontainers.exposeHostPorts(this.devserviceConfig.keycloak().port());
		
		addFixedExposedPort(devserviceConfig.port(), 80);
		
		// Tell the dev service how to know the container is ready. All 3 is likely overkill, but eh
		waitingFor(Wait.forHealthcheck());
		waitingFor(Wait.forLogMessage(".*Open QuarterMaster Web Server starting.*", 1));
		waitingFor(Wait.forHttp("/q/health").forResponsePredicate((String response)->{
			ObjectNode status;
			try {
				status = (ObjectNode) new ObjectMapper().readTree(response);
			} catch(Exception e) {
				return false;
			}
			return status.get("status").asText().equals("UP");
		}));
	}
	
	public Integer getPort() {
		return this.devserviceConfig.port();
	}
}
