package tech.ebp.oqm.lib.core.api.java.testUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.util.Base64;
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
	
	/**
	 * https://quarkus.io/guides/security-jwt#dealing-with-verification-keys
	 * @return
	 */
	public OqmCoreApiWebServiceContainer setupForPlainJwtAuth() {
		this.withEnv(Map.of(
			"mp.jwt.verify.publickey", Base64.getEncoder().encodeToString(JwtUtils.SIGNING_KEYPAIR.getPublic().getEncoded())
		));
		return this;
	}
}
