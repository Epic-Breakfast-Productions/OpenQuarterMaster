package tech.ebp.oqm.lib.core.api.quarkus.deployment;

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
		
		addExposedPorts(PORT);
		// Tell the dev service how to know the container is ready
		waitingFor(Wait.forLogMessage(".*Open QuarterMaster Web Server starting.*", 1));
		waitingFor(Wait.forHttp("/q/health"));
	}
	
	public Integer getPort() {
		return this.getMappedPort(PORT);
	}
}
