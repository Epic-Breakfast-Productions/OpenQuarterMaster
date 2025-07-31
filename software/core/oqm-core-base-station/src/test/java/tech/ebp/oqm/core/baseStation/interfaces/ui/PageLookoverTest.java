package tech.ebp.oqm.core.baseStation.interfaces.ui;

import tech.ebp.oqm.core.baseStation.testResources.testUsers.TestUser;
import tech.ebp.oqm.core.baseStation.testResources.testClasses.WebUiTest;
import tech.ebp.oqm.core.baseStation.testResources.testUsers.TestUserService;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@QuarkusTest
public class PageLookoverTest extends WebUiTest {

	private static List<Arguments> iterationsOnPage(String page){
		List<Arguments> output = new ArrayList<>();

		for(TestUser curTestUser : TestUserService.getInstance().getAllTestUsers()){
			output.add(Arguments.of(page, curTestUser, false));
			output.add(Arguments.of(page, curTestUser, true));
		}

		return output;
	}

	public static Stream<Arguments> pages(){
		List<Arguments> output = new ArrayList<>();

		output.addAll(iterationsOnPage("/overview"));
		output.addAll(iterationsOnPage("/storage"));
		output.addAll(iterationsOnPage("/items"));
		output.addAll(iterationsOnPage("/itemCategories"));
		output.addAll(iterationsOnPage("/itemCheckout"));
		output.addAll(iterationsOnPage("/you"));
		output.addAll(iterationsOnPage("/inventoryAdmin"));
		output.addAll(iterationsOnPage("/images"));
		output.addAll(iterationsOnPage("/files"));
		output.addAll(iterationsOnPage("/codes"));
		output.addAll(iterationsOnPage("/help"));

		return output.stream();
	}

	@ParameterizedTest
	@MethodSource("pages")
	public void lookoverTest(
		String pageEndpoint,
		TestUser testUser,
		boolean dataPresent
	) {
		log.info("Testing {} page can load with test user {} and data present={}", pageEndpoint, testUser, dataPresent);

		this.getLoggedInPage(testUser, pageEndpoint);
	}
}
