package com.ebp.openQuarterMaster.baseStation.testResources.testClasses;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.SAME_THREAD)//TODO:: remove this, when know how to make this work
public abstract class RunningServerTest extends WebServerTest {
	@BeforeEach
	public void beforeEach() {
		this.cleanup();
	}
	
	@AfterEach
	public void afterEach() {
		this.cleanup();
	}
	
	public void cleanup() {
	
	}
}
