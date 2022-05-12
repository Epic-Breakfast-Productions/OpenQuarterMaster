package com.ebp.openQuarterMaster.baseStation.testResources.testClasses;

import com.ebp.openQuarterMaster.baseStation.testResources.ui.WebDriverWrapper;
import org.junit.jupiter.api.Tag;

@Tag("ui")
public abstract class WebUiTest extends RunningServerTest {
	protected WebDriverWrapper webDriverWrapper = new WebDriverWrapper();
}
