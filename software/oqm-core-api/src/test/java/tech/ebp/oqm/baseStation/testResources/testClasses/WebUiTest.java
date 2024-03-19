package tech.ebp.oqm.baseStation.testResources.testClasses;

import org.junit.jupiter.api.Tag;
import tech.ebp.oqm.baseStation.testResources.ui.WebDriverWrapper;

@Tag("ui")
public abstract class WebUiTest extends RunningServerTest {
	protected WebDriverWrapper webDriverWrapper = new WebDriverWrapper();
}
