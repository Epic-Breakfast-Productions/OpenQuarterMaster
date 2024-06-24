package com.ebp.openQuarterMaster.testResources.testClasses;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static io.restassured.RestAssured.given;

@Slf4j
@Execution(ExecutionMode.SAME_THREAD)
//@ExtendWith(SeleniumRecordingTriggerExtension.class)
public abstract class RunningServerTest extends WebServerTest {

//	@Getter
//	TestUserService testUserService = TestUserService.getInstance();

	@BeforeEach
	public void beforeEach(TestInfo testInfo){
//		User adminUser = this.getTestUserService().getTestUser(true);
//		setupJwtCall(given(), this.getTestUserService().getUserToken(adminUser))
//			.basePath("")
//			.get("/api/v1/inventory/manage/db/refreshCache").then().statusCode(200);
//		setupJwtCall(given(), this.getTestUserService().getUserToken(adminUser))
//			.basePath("")
//			.put("/api/v1/inventory/manage/db/ensure/" + DEFAULT_TEST_DB_NAME).then().statusCode(200);

//		if(SeleniumGridServerManager.RECORD) {
//			TestResourceLifecycleManager.BROWSER_CONTAINER.beforeTest(
//				new OurTestDescription(testInfo)
//			);
//		}
	}
	
	@AfterEach
	public void afterEach(
		TestInfo testInfo
	) {
		log.info("Running after method for test {}", testInfo.getDisplayName());
		
		if(ConfigProvider.getConfig().getOptionalValue("quarkus.mongodb.connection-string", String.class).isEmpty()){
			log.info("Mongo not started.");
		} else {
//			MongoTestConnector.getInstance().clearDb();

			// this might be required for
//			if("intTest".equals(ConfigProvider.getConfig().getValue("quarkus.profile", String.class))){
//				setupJwtCall(given(), this.getTestUserService().getUserToken(this.getTestUserService().getTestUser(true)))
//					.basePath("")
//					.delete("/api/v1/inventory/manage/db/" + DEFAULT_TEST_DB_NAME+ "/clearDb").then().statusCode(200);
//			} else {
//				MongoTestConnector.getInstance().clearDb();
//			}
		}
		
//		if(SeleniumGridServerManager.RECORD) {
//			TestResourceLifecycleManager.BROWSER_CONTAINER.triggerRecord(
//				new OurTestDescription(testInfo),
//				//TODO:: actually pass something real https://stackoverflow.com/questions/71354431/junit5-get-results-from-test-in-aftereach
//				Optional.empty()
//			);
//		}
		findAndCleanupWebDriverWrapper();
		
		log.info("Completed after step.");
	}
	
	private Stream<Field> allFieldsForSelf() {
		return walkInheritanceTreeForSelf().flatMap( k -> Arrays.stream(k.getDeclaredFields()) );
	}
	
	private Stream<Class> walkInheritanceTreeForSelf() {
		return iterate( this.getClass(), k -> Optional.ofNullable(k.getSuperclass()) );
	}
	private <T> Stream<T> iterate( T seed, Function<T,Optional<T>> fetchNextFunction ) {
		Objects.requireNonNull(fetchNextFunction);
		
		Iterator<T> iterator = new Iterator<T>() {
			private Optional<T> t = Optional.ofNullable(seed);
			
			public boolean hasNext() {
				return t.isPresent();
			}
			
			public T next() {
				T v = t.get();
				
				t = fetchNextFunction.apply(v);
				
				return v;
			}
		};
		
		return StreamSupport.stream(
			Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED | Spliterator.IMMUTABLE),
			false
		);
	}
	
	private void findAndCleanupWebDriverWrapper() {
		log.info("Searching for WebDriverWrappers to cleanup: {}", this.getClass());
//		List<WebDriverWrapper> webDriverWrapperList = (
//			allFieldsForSelf().filter((Field curField)->{
//				log.debug("Field: {}", curField.getType());
//				return WebDriverWrapper.class.isAssignableFrom(curField.getType()) ||
//					   curField.getType().isAssignableFrom(TestUserService.class);
//			}).map((Field curField)->{
//				Object cur;
//				log.debug("WebDriverWrapper field: {}", curField.getType());
//				try {
//					if (!curField.canAccess(this)) {
//						curField.setAccessible(true);
//					}
//					cur = curField.get(this);
//				} catch(IllegalAccessException e) {
//					log.warn("Cannot access field: {}. ", curField, e);
//					return (WebDriverWrapper) null;
//				}
//				if(cur == null){
//					log.debug("Null value from field.");
//					return null;
//				}
//				log.debug("Value({}): {}", cur.getClass(), cur);
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
	}
}
