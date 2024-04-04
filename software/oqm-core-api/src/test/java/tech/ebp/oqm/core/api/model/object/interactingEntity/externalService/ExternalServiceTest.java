package tech.ebp.oqm.core.api.model.object.interactingEntity.externalService;

import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.core.api.model.rest.externalService.ExternalServiceSetupRequest;
import tech.ebp.oqm.core.api.model.testUtils.BasicTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public abstract class ExternalServiceTest<T extends ExternalService, R extends ExternalServiceSetupRequest> extends BasicTest {
	
	//TODO:: 382
//	@ParameterizedTest
//	@MethodSource("getUnchangedExtServices")
//	public void testUnchangedExtService(T externalService, R externalServiceSetupRequest) {
//		log.info("Testing unchanged: {} / {}", externalService, externalServiceSetupRequest);
//
//		assertFalse(externalService.changedGiven(externalServiceSetupRequest));
//	}
//
//	@ParameterizedTest
//	@MethodSource("getChangedExtServices")
//	public void testChangedExtService(T externalService, R externalServiceSetupRequest) {
//		log.info("Testing unchanged: {} / {}", externalService, externalServiceSetupRequest);
//
//		assertTrue(externalService.changedGiven(externalServiceSetupRequest));
//	}
	
	
}