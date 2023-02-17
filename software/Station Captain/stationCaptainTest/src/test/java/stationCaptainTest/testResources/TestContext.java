package stationCaptainTest.testResources;

import lombok.Data;
import lombok.NoArgsConstructor;
import stationCaptainTest.testResources.shellUtils.ShellProcessResults;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class TestContext {
	private ShellProcessResults shellProcessResults;
	private Map<String, Object> data = new HashMap<>();
}
