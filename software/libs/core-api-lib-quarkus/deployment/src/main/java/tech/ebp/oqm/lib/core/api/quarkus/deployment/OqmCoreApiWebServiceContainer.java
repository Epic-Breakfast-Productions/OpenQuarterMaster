package tech.ebp.oqm.lib.core.api.quarkus.deployment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

class OqmCoreApiWebServiceContainer extends GenericContainer<OqmCoreApiWebServiceContainer> {
	
	static final int PORT = 8123;
	
	public OqmCoreApiWebServiceContainer(DockerImageName image) {
		super(image);
	}
	
	@Override
	protected void configure() {
		withNetwork(Network.SHARED);
		withEnv("quarkus.http.port", ""+PORT);
		
		Testcontainers.exposeHostPorts(9328);//TODO:: get from config
		
		addExposedPorts(PORT);
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
