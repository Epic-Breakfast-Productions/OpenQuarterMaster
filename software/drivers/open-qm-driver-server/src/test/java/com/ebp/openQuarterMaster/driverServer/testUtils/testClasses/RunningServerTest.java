package com.ebp.openQuarterMaster.driverServer.testUtils.testClasses;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Slf4j
@Execution(ExecutionMode.SAME_THREAD)

//@ExtendWith(SeleniumRecordingTriggerExtension.class)
public abstract class RunningServerTest extends WebServerTest {
	
	@AfterEach
	public void afterEach(
		TestInfo testInfo
	) {
		log.info("Running after method.");
		
		log.info("Completed after step.");
	}
}
