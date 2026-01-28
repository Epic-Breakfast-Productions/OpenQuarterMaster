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
		//TODO:: use config to get port
		withEnv("quarkus.http.port", ""+PORT);
		
		Testcontainers.exposeHostPorts(this.devserviceConfig.keycloak().port());
		
		addExposedPorts(PORT);
		addFixedExposedPort(PORT, PORT);
		// Tell the dev service how to know the container is ready
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
		return this.getMappedPort(PORT);
	}
}
