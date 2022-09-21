package stationCaptainTest;

import io.cucumber.java.en.*;

import org.junit.jupiter.api.Assertions.*;

public class StepDefinitions {

    @Given("an example scenario")
    public void anExampleScenario() {
    }

    @When("all step definitions are implemented")
    public void allStepDefinitionsAreImplemented() throws InterruptedException {
        Thread.sleep(5_000);
    }

    @Then("the scenario passes")
    public void theScenarioPasses() {
    }

}
