package tech.ebp.oqm.baseStation.model.object.interactingEntity.externalService;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntityReference;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntityType;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.externalService.roles.RequestedRole;
import tech.ebp.oqm.baseStation.model.rest.auth.roles.ServiceRoles;
import tech.ebp.oqm.baseStation.model.rest.externalService.GeneralServiceSetupRequest;

import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
public class GeneralServiceTest<T extends ExternalService> extends ExternalServiceTest<GeneralService, GeneralServiceSetupRequest> {
	
	
	public static Stream<Arguments> getUnchangedExtServices() {
		GeneralService s = new GeneralService();
		s.setName(FAKER.name().name())
		 .setDescription(FAKER.lorem().paragraph())
		 .setDeveloperName(FAKER.name().name())
		 .setDeveloperEmail(FAKER.internet().emailAddress());
		
		GeneralServiceSetupRequest r = GeneralServiceSetupRequest.builder()
																 .name(s.getName())
																 .description(s.getDescription())
																 .developerName(s.getDeveloperName())
																 .developerEmail(s.getDeveloperEmail())
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
		 .setDeveloperEmail(FAKER.internet().emailAddress());
		
		return Stream.of(
			Arguments.of(s, GeneralServiceSetupRequest.builder()
													  .name(FAKER.name().name())
													  .description(s.getDescription())
													  .developerName(s.getDeveloperName())
													  .developerEmail(s.getDeveloperEmail())
													  .build()),
			Arguments.of(s, GeneralServiceSetupRequest.builder()
													  .name(s.getName())
													  .description(FAKER.lorem().paragraph())
													  .developerName(s.getDeveloperName())
													  .developerEmail(s.getDeveloperEmail())
													  .build()),
			Arguments.of(s, GeneralServiceSetupRequest.builder()
													  .name(s.getName())
													  .description(s.getDescription())
													  .developerName(FAKER.name().name())
													  .developerEmail(s.getDeveloperEmail())
													  .build()),
			Arguments.of(s, GeneralServiceSetupRequest.builder()
													  .name(s.getName())
													  .description(s.getDescription())
													  .developerName(s.getDeveloperName())
													  .developerEmail(FAKER.internet().emailAddress())
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