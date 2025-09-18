package tech.ebp.oqm.lib.core.api.java.testUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

public class OqmCoreApiWebServiceContainer extends GenericContainer<OqmCoreApiWebServiceContainer> {
	
	static final int PORT = 8123;
	
	public OqmCoreApiWebServiceContainer(DockerImageName image) {
		super(image);
	}
	
	@Override
	protected void configure() {
		this.withNetwork(Network.SHARED);
		//TODO:: use config to get port
		this.withEnv("quarkus.http.port", ""+PORT);
		this.withEnv("mp.messaging.outgoing.events-outgoing.enabled", "false");
		
		
		this.addExposedPorts(PORT);
		// Tell the dev service how to know the container is ready
		this.waitingFor(Wait.forLogMessage(".*Open QuarterMaster Web Server starting.*", 1));
		this.waitingFor(Wait.forHttp("/q/health").forResponsePredicate((String response)->{
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
	
	public OqmCoreApiWebServiceContainer setupForBasicAuth() {
		this.withEnv(Map.of(
			"quarkus.smallrye-jwt.enabled", "false",
			"quarkus.http.auth.basic", "true",
			"quarkus.security.users.embedded.enabled", "true",
			"quarkus.security.users.embedded.plain-text", "true",
			"quarkus.security.users.embedded.users.regularUser", "wow",
			"quarkus.security.users.embedded.users.adminUser", "wow",
			"quarkus.security.users.embedded.roles.regularUser", "inventoryView,inventoryEdit",
			"quarkus.security.users.embedded.roles.adminUser", "inventoryView,inventoryEdit,inventoryAdmin"
		));
		return this;
	}
}
