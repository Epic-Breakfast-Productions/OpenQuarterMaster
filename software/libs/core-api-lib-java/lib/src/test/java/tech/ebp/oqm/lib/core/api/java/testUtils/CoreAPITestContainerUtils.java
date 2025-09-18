package tech.ebp.oqm.lib.core.api.java.testUtils;

import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

public class CoreAPITestContainerUtils {
	private static final String MONGODB_DEVSERVICE_HOSTNAME = "oqm-dev-mongodb-server";
	
	
	public static MongoDBContainer getMongoContainer(){
		DockerImageName mongoImageName = DockerImageName.parse("mongo:7");
		
		MongoDBContainer mongoDBContainer = new MongoDBContainer(mongoImageName);
		mongoDBContainer.addExposedPorts();
		mongoDBContainer.withNetwork(Network.SHARED);
		mongoDBContainer.withNetworkAliases(MONGODB_DEVSERVICE_HOSTNAME);
		
		return mongoDBContainer;
	}
	
	
	public static OqmCoreApiWebServiceContainer getCoreApiContainer() {
		DockerImageName dockerImageName = DockerImageName.parse("docker.io/ebprod/oqm-core-api:3.2.0-DEV");
			
			OqmCoreApiWebServiceContainer container = new OqmCoreApiWebServiceContainer(dockerImageName)
														  .withAccessToHost(true)
														  .withEnv("quarkus.mongodb.connection-string", "mongodb://" + MONGODB_DEVSERVICE_HOSTNAME + ":27017")
														  .withNetwork(Network.SHARED);
			
			return container;
	}
}
