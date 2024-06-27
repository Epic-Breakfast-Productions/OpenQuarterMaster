package com.ebp.openQuarterMaster.testResources.lifecycle;

import com.ebp.openQuarterMaster.testResources.ui.PlaywrightSetup;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

public class TestCleanup implements TestExecutionListener {

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		TestExecutionListener.super.executionFinished(testIdentifier, testExecutionResult);

		PlaywrightSetup.getINSTANCE().close();
	}
}
