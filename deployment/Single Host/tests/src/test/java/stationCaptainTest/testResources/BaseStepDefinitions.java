package stationCaptainTest.testResources;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Scenario;
import lombok.Data;
import stationCaptainTest.testResources.config.ConfigReader;
import stationCaptainTest.testResources.config.TestRunConfig;

import java.io.IOException;

@Data
//@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseStepDefinitions {
	protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	protected static final TestRunConfig CONFIG;
	
	static {
		try {
			CONFIG = ConfigReader.getTestRunConfig();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Scenario scenario;
	private TestContext context;
	
	protected BaseStepDefinitions(TestContext context){
		this.context = context;
	}
	
	public abstract void setup(Scenario scenario);
}
