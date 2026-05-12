package tech.ebp.oqm.core.api.service.mongo;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.core.api.testResources.data.TestUserService;
import tech.ebp.oqm.core.api.testResources.testClasses.MongoHistoriedServiceTest;
import tech.ebp.oqm.core.api.model.units.CustomUnitEntry;
import tech.ebp.oqm.core.api.model.units.UnitUtils;

import jakarta.inject.Inject;
import tech.ebp.oqm.core.api.service.mongo.CustomUnitService;

import java.util.stream.Stream;

//TODO:: reimplement
//@Slf4j
//@QuarkusTest
//@QuarkusTestResource(TestResourceLifecycleManager.class)
//class CustomUnitServiceTest extends MongoHistoriedServiceTest<CustomUnitEntry, CustomUnitService> {
//
//	@Inject
//	CustomUnitService unitService;
//
//	@Inject
//	TestUserService testUserService;
//
//	@Override
//	protected CustomUnitEntry getTestObject() {
//		return null;
//	}
//
//	private static Stream<Arguments> unitsAsArgs() {
//		return UnitUtils.UNIT_LIST.stream().map(Arguments::of);
//	}
//	//TODO
////	@ParameterizedTest
////	@MethodSource("unitsAsArgs")
////	public void testCanStoreUnits(Unit<?> unit) {
////		log.info("Testing that units can be stored with {}", unit);
////		log.info("Unit name: {}", unit.getName());
////		log.info("Unit symbol: {}", unit.getSymbol());
////		log.info("Unit systemUnit: {}", unit.getSystemUnit());
////		log.info("Unit dimension: {}", unit.getDimension());
////
////		CustomUnitEntry original = new CustomUnitEntry(UnitCategory.Number, 0L, unit);
////
////		ObjectId id = this.unitService.add(
////			original,
////			this.testUserService.getTestUser(true, true)
////		);
////
////		assertNotNull(id);
////
////		CustomUnitEntry gotten = this.unitService.get(id);
////
////		assertEquals(original, gotten);
////		assertEquals(unit, gotten.getUnit());
////
////		Unit<?> newUnit = gotten.getUnit();
////
////		log.info("Returned unit: {}", newUnit);
////
////		log.info("new Unit name: {}", newUnit.getName());
////		log.info("new Unit symbol: {}", newUnit.getSymbol());
////		log.info("new Unit systemUnit: {}", newUnit.getSystemUnit());
////		log.info("new Unit dimension: {}", newUnit.getDimension());
////	}
////
////	@Test
////	public void testStoreCustomUnitsBase() {
////		NewBaseCustomUnitRequest nbcur = NewBaseCustomUnitRequest
////											 .builder()
////											 .dimension(ValidUnitDimension.amount)
////											 .unitCategory(UnitCategory.Number)
////											 .name(FAKER.name().name())
////											 .symbol(FAKER.food().dish())
////											 .build();
////
////		CustomUnitEntry newUnitEntry = nbcur.toCustomUnitEntry();
////
////		UnitUtils.registerAllUnits(newUnitEntry);
////
////		ObjectId id = this.unitService.add(newUnitEntry, this.testUserService.getTestUser(true, true));
////
////		CustomUnitEntry entryAdded = this.unitService.get(id);
////
////		assertEquals(newUnitEntry, entryAdded);
////	}
////
////	@AfterEach
////	public void cleanupUnits(){
////		UnitUtils.reInitUnitCollections();
////	}
//}