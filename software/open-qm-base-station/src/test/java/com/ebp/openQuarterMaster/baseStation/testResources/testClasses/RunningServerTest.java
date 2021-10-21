package com.ebp.openQuarterMaster.baseStation.testResources.testClasses;

import com.ebp.openQuarterMaster.baseStation.testResources.TestResourceLifecycleManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.IOException;

@Execution(ExecutionMode.SAME_THREAD)//TODO:: remove this, when know how to make this work
public abstract class RunningServerTest extends WebServerTest {
	@BeforeEach
	public void beforeEach() throws IOException {
		this.cleanup();
	}
	
	@AfterEach
	public void afterEach() throws IOException {
		this.cleanup();
	}
	
	public void cleanup() throws IOException {

	}
}
