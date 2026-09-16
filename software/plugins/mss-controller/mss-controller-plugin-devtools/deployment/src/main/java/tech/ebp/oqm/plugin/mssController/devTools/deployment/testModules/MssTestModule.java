package tech.ebp.oqm.plugin.mssController.devTools.deployment.testModules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.eclipse.microprofile.config.ConfigProvider;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import tech.ebp.oqm.plugin.mssController.lib.command.response.ModuleInfo;

import java.util.HashMap;
import java.util.Map;

public abstract class MssTestModule<SELF extends MssTestModule<SELF>> extends GenericContainer<SELF> {

	protected ModuleInfo moduleInfo;
	protected Map<String, String> appConfig = new HashMap<>();

	protected MssTestModule(ModuleInfo moduleInfo) {
		super(DockerImageName.parse("ebprod/oqm-plugin-mss_controller-test_module_server:" + ConfigProvider.getConfig().getValue("quarkus.application.version", String.class)));
		this.moduleInfo = moduleInfo;
	}

	static final int PORT = 8123;

	public Map<String, String> getAppConfig() {
		return this.appConfig;
	}


	@Override
	protected void configure() {
		withNetwork(Network.SHARED);
		withEnv("quarkus.http.port", ""+PORT);
		addEnv("moduleConfig.serialId", this.moduleInfo.getSerialId());
		addEnv("moduleConfig.manufactureDate", this.moduleInfo.getManufactureDate());
		addEnv("moduleConfig.specVersion", this.moduleInfo.getSpecVersion());
		addEnv("moduleConfig.numBlocks", "" + this.moduleInfo.getNumBlocks());

		addExposedPorts(PORT);
		// Tell the dev service how to know the container is ready
		waitingFor(Wait.forLogMessage(".*MSS Test Module Server started.*", 1));
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
