package tech.ebp.oqm.lib.core.api.java.testUtils.testClases;

import lombok.Getter;
import org.testcontainers.containers.MongoDBContainer;
import tech.ebp.oqm.lib.core.api.java.config.CoreApiConfig;
import tech.ebp.oqm.lib.core.api.java.testUtils.TestContainerUtils;
import tech.ebp.oqm.lib.core.api.java.testUtils.OqmCoreApiWebServiceContainer;

import java.net.URI;
import java.net.URISyntaxException;

@Getter
public abstract class RunningServerTest {
	protected static MongoDBContainer mongoDBContainer = TestContainerUtils.getMongoContainer();
	protected static OqmCoreApiWebServiceContainer coreApiContainer = TestContainerUtils.getCoreApiContainer();
	
	public static void startContainers() {
		mongoDBContainer.start();
		coreApiContainer.start();
	}
	
	public static void stopContainers() {
		mongoDBContainer.stop();
		coreApiContainer.stop();
	}
	
	public CoreApiConfig.CoreApiConfigBuilder getCoreApiConfig(boolean https) {
		try {
			return CoreApiConfig.builder()
					   .baseUri(new URI("http"+(https?"s":"")+"://" + coreApiContainer.getHost() + ":" + coreApiContainer.getPort(https)));
		} catch(URISyntaxException e) {
			throw new RuntimeException("Failed to create uri for core api.", e);
		}
	}
	
	public CoreApiConfig.CoreApiConfigBuilder getCoreApiConfig() {
		return getCoreApiConfig(false);
	}
	
}
