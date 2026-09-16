package tech.ebp.oqm.lib.core.object.interactingEntity.externalService;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.lib.core.rest.externalService.ExternalServiceSetupRequest;
import tech.ebp.oqm.lib.core.testUtils.BasicTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public abstract class ExternalServiceTest<T extends ExternalService, R extends ExternalServiceSetupRequest> extends BasicTest {
	
	
	@ParameterizedTest
	@MethodSource("getUnchangedExtServices")
	public void testUnchangedExtService(T externalService, R externalServiceSetupRequest) {
		log.info("Testing unchanged: {} / {}", externalService, externalServiceSetupRequest);
		
		assertFalse(externalService.changedGiven(externalServiceSetupRequest));
	}
	
	@ParameterizedTest
	@MethodSource("getChangedExtServices")
	public void testChangedExtService(T externalService, R externalServiceSetupRequest) {
		log.info("Testing unchanged: {} / {}", externalService, externalServiceSetupRequest);
		
		assertTrue(externalService.changedGiven(externalServiceSetupRequest));
	}
	
	
}