package stationCaptainTest.stepDefinitions.features;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import stationCaptainTest.testResources.BaseStepDefinitions;
import stationCaptainTest.testResources.TestContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Installers extends BaseStepDefinitions {
	
	public Installers(TestContext context) {
		super(context);
	}
	
	@When("the command to make the installers are made")
	public void the_command_to_make_the_installers_are_made() {
		
		// Write code here that turns the phrase above into concrete actions
		throw new io.cucumber.java.PendingException();
	}
	
	@Then("the following installers were created:")
	public void the_following_installers_were_created(io.cucumber.datatable.DataTable dataTable) {
		// Write code here that turns the phrase above into concrete actions
		// For automatic transformation, change DataTable to one of
		// E, List<E>, List<List<E>>, List<Map<K,V>>, Map<K,V> or
		// Map<K, List<V>>. E,K,V must be a String, Integer, Float,
		// Double, Byte, Short, Long, BigInteger or BigDecimal.
		//
		// For other transformations you can register a DataTableType.
		throw new io.cucumber.java.PendingException();
	}
}
