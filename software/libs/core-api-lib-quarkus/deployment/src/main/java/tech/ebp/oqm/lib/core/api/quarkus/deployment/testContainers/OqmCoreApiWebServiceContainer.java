package tech.ebp.oqm.lib.core.api.quarkus.deployment.testContainers;

import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import tech.ebp.oqm.lib.core.api.quarkus.deployment.config.CoreApiLibBuildTimeConfig;

import java.util.Map;

/**
 * Container for the Open QuarterMaster Core API web service.
 */
public class OqmCoreApiWebServiceContainer extends GenericContainer<OqmCoreApiWebServiceContainer> {
	
	private final CoreApiLibBuildTimeConfig.DevserviceConfig devserviceConfig;
	private final Map<String, String> mongoConnectionInfo;
	private final Map<String, String> kafkaConnectionInfo;
	
	/**
	 * Initializes the container
	 * @param devserviceConfig The devservice config to use to set this container up.
	 */
	public OqmCoreApiWebServiceContainer(
		CoreApiLibBuildTimeConfig.DevserviceConfig devserviceConfig,
		Map<String, String> mongoConnectionInfo,
		Map<String, String> kafkaConnectionInfo
	) {
		super(devserviceConfig.image().toTestContainerImageName());
		this.devserviceConfig = devserviceConfig;
		this.mongoConnectionInfo = mongoConnectionInfo;
		this.kafkaConnectionInfo = kafkaConnectionInfo;
	}
	
	@Override
	protected void configure() {
		//configure network
		withNetwork(Network.SHARED);
		Testcontainers.exposeHostPorts(this.devserviceConfig.keycloak().port());
		addFixedExposedPort(devserviceConfig.port(), 80);
		//configure env
		this.withEnv(mongoConnectionInfo);
		this.withEnv(kafkaConnectionInfo);
		
		// Tell the dev service how to know the container is ready. All 3 is likely overkill, but eh
		this.waitingFor(Wait.forHealthcheck());
		this.waitingFor(Wait.forLogMessage(".*oqm-core-api .* started.*", 1));
		
		//don't need to do this, the docker healthcheck covers this. Not included as logs a superfluous stacktrace at startup.
//		this.waitingFor(Wait.forHttp("/q/health").forResponsePredicate((String response)->{
//			ObjectNode status;
//			try {
//				status = (ObjectNode) new ObjectMapper().readTree(response);
//			} catch(Exception e) {
//				return false;
//			}
//			return status.get("status").asText().equals("UP");
//		}));
	}
	
	/**
	 * Gets the port that the dev service is listening on.
	 * @return the port that the dev service is listening on.
	 */
	public Integer getPort() {
		return this.devserviceConfig.port();
	}
}
