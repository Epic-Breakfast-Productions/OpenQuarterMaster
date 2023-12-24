package stationCaptainTest.testResources;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Scenario;
import lombok.Data;
import stationCaptainTest.testResources.config.ConfigReader;
import stationCaptainTest.testResources.config.TestRunConfig;

@Data
//@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseStepDefinitions {
	protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	protected static final TestRunConfig CONFIG = ConfigReader.getTestRunConfig();
	
	private Scenario scenario;
	private TestContext context;
	
	protected BaseStepDefinitions(TestContext context){
		this.context = context;
	}
	
	public abstract void setup(Scenario scenario);
}
