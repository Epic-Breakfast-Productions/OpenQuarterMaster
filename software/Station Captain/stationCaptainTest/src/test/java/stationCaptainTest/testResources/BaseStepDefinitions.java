package stationCaptainTest.testResources;

import io.cucumber.java.Scenario;
import lombok.Data;

@Data
//@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseStepDefinitions {
	private Scenario scenario;
	private TestContext context;
	
	protected BaseStepDefinitions(TestContext context){
		this.context = context;
	}
	
	public abstract void setup(Scenario scenario);
}
