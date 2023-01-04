package tech.ebp.oqm.lib.core.object.interactingEntity.externalService;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.lib.core.object.interactingEntity.externalService.roles.RequestedRole;
import tech.ebp.oqm.lib.core.rest.auth.roles.ServiceRoles;
import tech.ebp.oqm.lib.core.rest.externalService.GeneralServiceSetupRequest;
import tech.ebp.oqm.lib.core.testUtils.BasicTest;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;

@Slf4j
public class GeneralServiceTest<T extends ExternalService> extends ExternalServiceTest<GeneralService, GeneralServiceSetupRequest> {
	
	
	public static Stream<Arguments> getUnchangedExtServices() {
		GeneralService s = new GeneralService();
		s.setName(FAKER.name().name())
		 .setDescription(FAKER.lorem().paragraph())
		 .setDeveloperName(FAKER.name().name())
		 .setDeveloperEmail(FAKER.internet().emailAddress())
		 .setRequestedRoles(Set.of(
			 new RequestedRole(ServiceRoles.SERVICE_ROLES.get(0), "To do the thing", false)
		 ));
		
		GeneralServiceSetupRequest r = GeneralServiceSetupRequest.builder()
																 .name(s.getName())
																 .description(s.getDescription())
																 .developerName(s.getDeveloperName())
																 .developerEmail(s.getDeveloperEmail())
																 .requestedRoles(s.getRequestedRoles())
																 .build();
		
		return Stream.of(
			Arguments.of(s, r)
		);
	}
	
	public static Stream<Arguments> getChangedExtServices() {
		GeneralService s = new GeneralService();
		s.setName(FAKER.name().name())
		 .setDescription(FAKER.lorem().paragraph())
		 .setDeveloperName(FAKER.name().name())
		 .setDeveloperEmail(FAKER.internet().emailAddress())
		 .setRequestedRoles(Set.of(
			 new RequestedRole(ServiceRoles.SERVICE_ROLES.get(0), "To do the thing", false)
		 ));
		
		return Stream.of(
			Arguments.of(s, GeneralServiceSetupRequest.builder()
													  .name(FAKER.name().name())
													  .description(s.getDescription())
													  .developerName(s.getDeveloperName())
													  .developerEmail(s.getDeveloperEmail())
													  .requestedRoles(s.getRequestedRoles())
													  .build()),
			Arguments.of(s, GeneralServiceSetupRequest.builder()
													  .name(s.getName())
													  .description(FAKER.lorem().paragraph())
													  .developerName(s.getDeveloperName())
													  .developerEmail(s.getDeveloperEmail())
													  .requestedRoles(s.getRequestedRoles())
													  .build()),
			Arguments.of(s, GeneralServiceSetupRequest.builder()
													  .name(s.getName())
													  .description(s.getDescription())
													  .developerName(FAKER.name().name())
													  .developerEmail(s.getDeveloperEmail())
													  .requestedRoles(s.getRequestedRoles())
													  .build()),
			Arguments.of(s, GeneralServiceSetupRequest.builder()
													  .name(s.getName())
													  .description(s.getDescription())
													  .developerName(s.getDeveloperName())
													  .developerEmail(FAKER.internet().emailAddress())
													  .requestedRoles(s.getRequestedRoles())
													  .build()),
			Arguments.of(s, GeneralServiceSetupRequest.builder()
													  .name(s.getName())
													  .description(s.getDescription())
													  .developerName(s.getDeveloperName())
													  .developerEmail(s.getDeveloperEmail())
													  .requestedRoles(Set.of(
														  new RequestedRole(ServiceRoles.SERVICE_ROLES.get(1), "To do the thing 2", true)
													  ))
													  .build())
		);
	}
}