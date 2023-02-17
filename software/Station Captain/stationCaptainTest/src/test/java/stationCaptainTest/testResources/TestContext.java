package stationCaptainTest.testResources;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class TestContext {
	private Map<String, Object> data = new HashMap<>();
}
