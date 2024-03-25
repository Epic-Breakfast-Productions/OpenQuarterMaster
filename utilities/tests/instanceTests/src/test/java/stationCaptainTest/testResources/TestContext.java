package stationCaptainTest.testResources;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Data
@NoArgsConstructor
public class TestContext implements Closeable {
	
	private Map<String, Object> data = new HashMap<>();
	
	@Override
	public void close() {
	}
}
