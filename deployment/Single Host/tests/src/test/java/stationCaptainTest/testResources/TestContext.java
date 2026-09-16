package stationCaptainTest.testResources;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import stationCaptainTest.testResources.shellUtils.ShellProcessResults;
import stationCaptainTest.testResources.snhConnector.CommandResult;
import stationCaptainTest.testResources.snhConnector.SnhConnector;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Data
@NoArgsConstructor
public class TestContext implements Closeable {
	
	private SnhConnector<?> snhConnector;
	private CommandResult commandResult;
	private Map<String, Object> data = new HashMap<>();
	
	{
		try {
			this.snhConnector = SnhConnector.fromConfig();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void close() throws IOException {
		if(this.snhConnector != null){
			this.snhConnector.close();
		}
	}
}
