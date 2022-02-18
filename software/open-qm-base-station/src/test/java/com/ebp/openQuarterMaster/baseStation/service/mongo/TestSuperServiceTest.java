package com.ebp.openQuarterMaster.baseStation.service.mongo;

import com.ebp.openQuarterMaster.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import com.ebp.openQuarterMaster.baseStation.testResources.testClasses.MongoServiceTest;
import com.ebp.openQuarterMaster.lib.core.test.TestOne;
import com.ebp.openQuarterMaster.lib.core.test.TestSuper;
import com.ebp.openQuarterMaster.lib.core.test.TestTwo;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.inject.Inject;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
class TestSuperServiceTest extends MongoServiceTest<TestSuper, TestSuperService> {
	
	@Inject
	TestSuperService testSuperService;
	
	@Override
	protected TestSuper getTestObject() {
		return null;
	}
	
	public static Stream<Arguments> getObjs() {
		return Stream.of(
			Arguments.of(new TestOne("something")),
			Arguments.of(new TestTwo(3))
		);
	}
	
	@ParameterizedTest
	@MethodSource("getObjs")
	public void persistenceTest(TestSuper<?> obj) {
		log.info("Object: {}", obj);
		log.info("Class: {}", obj.getClass());
		log.info("Original obj type: {}", obj.getType());
		ObjectId id = testSuperService.add(obj, null);
		log.info("Object id: {}", id);
		
		TestSuper<?> gotten = testSuperService.get(id);
		log.info("Deserialized object: {}", obj);
		log.info("Deserialized class: {}", obj.getClass());
		log.info("Deserialized obj type: {}", gotten.getType());
		assertEquals(obj, gotten);
	}
}