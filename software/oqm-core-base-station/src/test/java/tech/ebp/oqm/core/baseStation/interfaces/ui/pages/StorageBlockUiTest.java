package tech.ebp.oqm.core.baseStation.interfaces.ui.pages;

import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.baseStation.testResources.testClasses.WebUiTest;

@Slf4j
@QuarkusTest
public class StorageBlockUiTest extends WebUiTest {

	@Test
	public void testAddStorageBlock(){
		this.getLoggedInPage(this.getTestUserService().getTestUser(), "/storage");
	}
}
