package com.ebp.openQuarterMaster.baseStation.testResources.testClasses;

import com.ebp.openQuarterMaster.baseStation.service.mongo.MongoService;
import com.ebp.openQuarterMaster.baseStation.testResources.data.TestUserService;
import com.ebp.openQuarterMaster.baseStation.testResources.lifecycleManagers.OurTestDescription;
import com.ebp.openQuarterMaster.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import com.ebp.openQuarterMaster.baseStation.testResources.ui.WebDriverWrapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Execution(ExecutionMode.SAME_THREAD)
public abstract class RunningServerTest extends WebServerTest {
	
	//TODO:: this with params
	@AfterEach
	public void afterEach(
		TestInfo testInfo
	) {
		log.info("Running after method.");
		findAndCleanupMongoServices();
		findAndCleanupWebDriverWrapper();
		
		TestResourceLifecycleManager.triggerRecord(
			new OurTestDescription(testInfo),
			//TODO:: actually pass something real https://stackoverflow.com/questions/71354431/junit5-get-results-from-test-in-aftereach
			Optional.empty()
		);
		
		log.info("Completed after step.");
	}
	
	private void findAndCleanupWebDriverWrapper() {
		log.info("Searching for WebDriverWrappers to cleanup: {}", this.getClass());
		List<WebDriverWrapper> webDriverWrapperList = (
			Arrays.stream(this.getClass().getDeclaredFields()).filter((Field curField)->{
				log.debug("Field: {}", curField.getType());
				return WebDriverWrapper.class.isAssignableFrom(curField.getType()) ||
					   curField.getType().isAssignableFrom(TestUserService.class);
			}).map((Field curField)->{
				Object cur;
				log.debug("WebDriverWrapper field: {}", curField.getType());
				try {
					if (!curField.canAccess(this)) {
						curField.setAccessible(true);
					}
					cur = curField.get(this);
				} catch(IllegalAccessException e) {
					log.warn("Cannot access field: {}. ", curField, e);
					return (WebDriverWrapper) null;
				}
				log.debug("Value: {}", cur.getClass());
				
				if (WebDriverWrapper.class.isAssignableFrom(cur.getClass())) {
					log.debug("Was regular WebDriverWrapper!");
					return (WebDriverWrapper) cur;
				}
				log.warn("Was not a service we recognize!");
				return (WebDriverWrapper) null;
			}).collect(Collectors.toList())
		);
		log.info("Found {} web driver wrappers.", webDriverWrapperList.size());
		
		for (WebDriverWrapper curWrapper : webDriverWrapperList) {
			if (curWrapper == null) {
				log.debug("Null web driver wrapper!");
				continue;
			}
			curWrapper.cleanup();
		}
	}
	
	private void findAndCleanupMongoServices() {
		log.info("Searching for MongoServices to use in cleanup: {}", this.getClass());
		List<MongoService> svcList = (
			Arrays.stream(this.getClass().getDeclaredFields()).filter((Field curField)->{
				log.debug("Field: {}", curField.getType());
				return MongoService.class.isAssignableFrom(curField.getType()) ||
					   curField.getType().isAssignableFrom(TestUserService.class);
			}).map((Field curField)->{
				Object cur;
				log.debug("Mongo service field: {}", curField.getType());
				try {
					if (!curField.canAccess(this)) {
						curField.setAccessible(true);
					}
					cur = curField.get(this);
				} catch(IllegalAccessException e) {
					log.warn("Cannot access field: {}. ", curField, e);
					return (MongoService) null;
				}
				log.debug("Value: {}", cur);
				
				if (MongoService.class.isAssignableFrom(cur.getClass())) {
					log.debug("Was regular MongoService!");
					return (MongoService) cur;
				} else if (cur instanceof TestUserService) {
					log.debug("Was testUser service!");
					return ((TestUserService) cur).getUserService();
				}
				log.warn("Was not a service we recognize!");
				return (MongoService) null;
			}).collect(Collectors.toList())
		);
		log.info("Found {} services to cleanup entries from: {}", svcList.size(), svcList);
		this.cleanup(svcList.toArray(new MongoService[svcList.size()]));
	}
	
	public void cleanup(MongoService... services) {
		log.info("Cleaning up test env.");
		
		long totalNumCleaned = 0;
		for (MongoService service : services) {
			if (service == null) {
				log.debug("Null service!");
				continue;
			}
			long numCleaned = service.removeAll();
			totalNumCleaned += numCleaned;
			log.info("Cleaned {} entries from service: {}", numCleaned, service.getClass());
		}
		log.info("Cleaned a total of {} entries from {} services.", totalNumCleaned, services.length);
	}
}
