package stationCaptainTest.testResources;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import stationCaptainTest.testResources.shellUtils.ShellProcessResults;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Data
@NoArgsConstructor
public class TestContext implements Closeable {
	private ShellProcessResults shellProcessResults = null;
	private Map<String, Object> data = new HashMap<>();
	private GenericContainer<?> runningContainer = null;
	private Container.ExecResult containerExecResult = null;
	
	@Override
	public void close() throws IOException {
		try(
			GenericContainer<?> container = this.getRunningContainer();
			){
			log.info("Container was started? {}", container != null);
		}
	}
}
