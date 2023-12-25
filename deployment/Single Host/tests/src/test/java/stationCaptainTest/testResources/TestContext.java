package stationCaptainTest.testResources;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import stationCaptainTest.testResources.shellUtils.ShellProcessResults;
import stationCaptainTest.testResources.snhConnector.SnhConnector;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Data
@NoArgsConstructor
public class TestContext implements Closeable {
	
	private SnhConnector snhConnector;
	
	private ShellProcessResults shellProcessResults = null;
	private Map<String, Object> data = new HashMap<>();
	private GenericContainer<?> runningContainer = null;
	private Container.ExecResult containerExecResult = null;
	private String installer = System.getProperty("test.installer", "deb");
	private String os = System.getProperty("test.os", "ubuntu");
	
	@Override
	public void close() throws IOException {
		if(this.snhConnector != null){
			this.snhConnector.close();
		}
		
		try (
			GenericContainer<?> container = this.getRunningContainer();
		) {
			log.info("Container was started? {}", container != null);
		}
	}
}
