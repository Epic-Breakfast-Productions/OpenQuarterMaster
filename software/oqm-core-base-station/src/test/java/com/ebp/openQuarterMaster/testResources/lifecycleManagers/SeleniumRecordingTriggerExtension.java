package com.ebp.openQuarterMaster.testResources.lifecycleManagers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

@Slf4j
public class SeleniumRecordingTriggerExtension implements AfterEachCallback {
	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		log.info("Hit afterEachCallback");
//
//		log.info("Running after method.");
//		findAndCleanupMongoServices(context.getRequiredTestInstance());
//		findAndCleanupWebDriverWrapper(context.getRequiredTestInstance());
//		log.info("Completed after step.");
//
//		TestResourceLifecycleManager.triggerRecord(
//			new OurTestDescription(context),
//			context.getExecutionException()
//		);
	}
	
//
//	private static void findAndCleanupWebDriverWrapper(Object testInstance) {
//		log.info("Searching for WebDriverWrappers to cleanup: {}", testInstance.getClass());
//		List<WebDriverWrapper> webDriverWrapperList = (
//			Arrays.stream(testInstance.getClass().getDeclaredFields()).filter((Field curField)->{
//				log.debug("Field: {}", curField.getType());
//				return WebDriverWrapper.class.isAssignableFrom(curField.getType()) ||
//					   curField.getType().isAssignableFrom(TestUserService.class);
//			}).map((Field curField)->{
//				Object cur;
//				log.debug("WebDriverWrapper field: {}", curField.getType());
//				try {
//					if (!curField.canAccess(testInstance)) {
//						curField.setAccessible(true);
//					}
//					cur = curField.get(testInstance);
//				} catch(IllegalAccessException e) {
//					log.warn("Cannot access field: {}. ", curField, e);
//					return (WebDriverWrapper) null;
//				}
//				log.debug("Value: {}", cur.getClass());
//
//				if (WebDriverWrapper.class.isAssignableFrom(cur.getClass())) {
//					log.debug("Was regular WebDriverWrapper!");
//					return (WebDriverWrapper) cur;
//				}
//				log.warn("Was not a service we recognize!");
//				return (WebDriverWrapper) null;
//			}).collect(Collectors.toList())
//		);
//		log.info("Found {} web driver wrappers.", webDriverWrapperList.size());
//
//		for (WebDriverWrapper curWrapper : webDriverWrapperList) {
//			if (curWrapper == null) {
//				log.debug("Null web driver wrapper!");
//				continue;
//			}
//			curWrapper.cleanup();
//		}
//	}
//
//	private static void findAndCleanupMongoServices(Object testInstance) {
//		log.info("Searching for MongoServices to use in cleanup: {}", testInstance.getClass());
//		List<MongoService> svcList = (
//			Arrays.stream(testInstance.getClass().getDeclaredFields()).filter((Field curField)->{
//				log.debug("Field: {}", curField.getType());
//				return MongoService.class.isAssignableFrom(curField.getType()) ||
//					   curField.getType().isAssignableFrom(TestUserService.class);
//			}).map((Field curField)->{
//				Object cur;
//				log.debug("Mongo service field: {}", curField.getType());
//				try {
//					if (!curField.canAccess(testInstance)) {
//						curField.setAccessible(true);
//					}
//					cur = curField.get(testInstance);
//				} catch(IllegalAccessException e) {
//					log.warn("Cannot access field: {}. ", curField, e);
//					return (MongoService) null;
//				}
//				log.debug("Value: {}", cur);
//
//				if (MongoService.class.isAssignableFrom(cur.getClass())) {
//					log.debug("Was regular MongoService!");
//					return (MongoService) cur;
//				} else if (cur instanceof TestUserService) {
//					log.debug("Was testUser service!");
//					return ((TestUserService) cur).getUserService();
//				}
//				log.warn("Was not a service we recognize!");
//				return (MongoService) null;
//			}).collect(Collectors.toList())
//		);
//		log.info("Found {} services to cleanup entries from: {}", svcList.size(), svcList);
//		cleanup(svcList.toArray(new MongoService[svcList.size()]));
//	}
//
//	public static void cleanup(MongoService... services) {
//		log.info("Cleaning up test env.");
//
//		long totalNumCleaned = 0;
//		for (MongoService service : services) {
//			if (service == null) {
//				log.debug("Null service!");
//				continue;
//			}
//			long numCleaned = service.removeAll();
//			totalNumCleaned += numCleaned;
//			log.info("Cleaned {} entries from service: {}", numCleaned, service.getClass());
//		}
//		log.info("Cleaned a total of {} entries from {} services.", totalNumCleaned, services.length);
//	}
}
