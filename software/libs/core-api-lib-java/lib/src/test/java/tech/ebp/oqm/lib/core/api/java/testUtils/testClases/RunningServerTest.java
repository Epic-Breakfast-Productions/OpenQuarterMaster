package tech.ebp.oqm.lib.core.api.java.testUtils.testClases;

import lombok.Getter;
import org.junit.jupiter.api.AfterAll;
import org.testcontainers.containers.MongoDBContainer;
import tech.ebp.oqm.lib.core.api.java.auth.OqmCredentials;
import tech.ebp.oqm.lib.core.api.java.config.CoreApiConfig;
import tech.ebp.oqm.lib.core.api.java.testUtils.CoreAPITestContainerUtils;
import tech.ebp.oqm.lib.core.api.java.testUtils.OqmCoreApiWebServiceContainer;

import java.net.URI;
import java.net.URISyntaxException;

@Getter
public abstract class RunningServerTest {
	protected static MongoDBContainer mongoDBContainer = CoreAPITestContainerUtils.getMongoContainer();
	protected static OqmCoreApiWebServiceContainer coreApiContainer = CoreAPITestContainerUtils.getCoreApiContainer();
	
	public static void startContainers() {
		mongoDBContainer.start();
		coreApiContainer.start();
	}
	
	public static void stopContainers() {
		mongoDBContainer.stop();
		coreApiContainer.stop();
	}
	
	public CoreApiConfig getCoreApiConfig(boolean https) {
		try {
			return CoreApiConfig.builder()
					   .baseUri(new URI("http"+(https?"s":"")+"://" + coreApiContainer.getHost() + ":" + coreApiContainer.getPort(https)))
					   .build();
		} catch(URISyntaxException e) {
			throw new RuntimeException("Failed to create uri for core api.", e);
		}
	}
	
	public CoreApiConfig getCoreApiConfig() {
		return getCoreApiConfig(false);
	}
	
	public abstract OqmCredentials getCredentials();
}
