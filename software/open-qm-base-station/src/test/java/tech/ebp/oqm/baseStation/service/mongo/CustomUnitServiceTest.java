package tech.ebp.oqm.baseStation.service.mongo;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.baseStation.testResources.data.TestUserService;
import tech.ebp.oqm.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.baseStation.testResources.testClasses.MongoHistoriedServiceTest;
import tech.ebp.oqm.lib.core.units.CustomUnitEntry;
import tech.ebp.oqm.lib.core.units.UnitCategory;
import tech.ebp.oqm.lib.core.units.UnitUtils;

import javax.inject.Inject;
import javax.measure.Unit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
class CustomUnitServiceTest extends MongoHistoriedServiceTest<CustomUnitEntry, CustomUnitService> {
	
	@Inject
	CustomUnitService unitService;
	
	@Inject
	TestUserService testUserService;
	
	@Override
	protected CustomUnitEntry getTestObject() {
		return null;
	}
	
	private static Stream<Arguments> unitsAsArgs() {
		return UnitUtils.UNIT_LIST.stream().map(Arguments::of);
	}
	
	@ParameterizedTest
	@MethodSource("unitsAsArgs")
	public void testCanStoreUnits(Unit<?> unit){
		log.info("Testing that units can be stored with {}", unit);
		log.info("Unit name: {}", unit.getName());
		log.info("Unit symbol: {}", unit.getSymbol());
		log.info("Unit systemUnit: {}", unit.getSystemUnit());
		log.info("Unit dimension: {}", unit.getDimension());
		
		CustomUnitEntry original = new CustomUnitEntry(UnitCategory.Number, unit);
		
		ObjectId id = this.unitService.add(
			original,
			this.testUserService.getTestUser(true, true)
		);
		
		assertNotNull(id);
		
		CustomUnitEntry gotten = this.unitService.get(id);
		
		assertEquals(original, gotten);
		assertEquals(unit, gotten.getUnit());
		
		Unit<?> newUnit = gotten.getUnit();
		
		log.info("Returned unit: {}", newUnit);
		
		log.info("new Unit name: {}", newUnit.getName());
		log.info("new Unit symbol: {}", newUnit.getSymbol());
		log.info("new Unit systemUnit: {}", newUnit.getSystemUnit());
		log.info("new Unit dimension: {}", newUnit.getDimension());
	}
	
	
}