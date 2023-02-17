package stationCaptainTest.testResources;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseStepDefinitions {
	private TestContext context;
}
