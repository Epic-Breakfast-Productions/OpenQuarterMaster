package tech.ebp.oqm.baseStation.model.object.interactingEntity.externalService;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntityReference;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntityType;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.externalService.ExternalService;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.externalService.plugin.PluginService;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.externalService.plugin.components.nav.NavItem;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.externalService.roles.RequestedRole;
import tech.ebp.oqm.baseStation.model.rest.auth.roles.ServiceRoles;
import tech.ebp.oqm.baseStation.model.rest.externalService.PluginServiceSetupRequest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
public class PluginServiceTest<T extends ExternalService> extends ExternalServiceTest<PluginService, PluginServiceSetupRequest> {
	
	
	public static Stream<Arguments> getUnchangedExtServices() throws MalformedURLException {
		PluginService s = new PluginService();
		s.setName(FAKER.name().name())
		 .setDescription(FAKER.lorem().paragraph())
		 .setDeveloperName(FAKER.name().name())
		 .setDeveloperEmail(FAKER.internet().emailAddress())
		;
		s.setDisabledPageComponents(
			List.of(
				NavItem.builder()
					   .name(FAKER.name().name())
					   .itemText(FAKER.lorem().sentence())
					   .itemUrl(new URL("https://" + FAKER.internet().url()))
					   .build()
			)
		);
		s.setEnabledPageComponents(
			List.of(
				NavItem.builder()
					   .name(FAKER.name().name())
					   .itemText(FAKER.lorem().sentence())
					   .itemUrl(new URL("https://" + FAKER.internet().url()))
					   .build()
			)
		);
		
		PluginServiceSetupRequest r = PluginServiceSetupRequest.builder()
															   .name(s.getName())
															   .description(s.getDescription())
															   .developerName(s.getDeveloperName())
															   .developerEmail(s.getDeveloperEmail())
															   .pageComponents(
																   List.of(
																	   s.getDisabledPageComponents().get(0),
																	   s.getEnabledPageComponents().get(0)
																   )
															   )
															   .build();
		
		return Stream.of(
			Arguments.of(s, r)
		);
	}
	
	public static Stream<Arguments> getChangedExtServices() throws MalformedURLException {
		PluginService s = new PluginService();
		s.setName(FAKER.name().name())
		 .setDescription(FAKER.lorem().paragraph())
		 .setDeveloperName(FAKER.name().name())
		 .setDeveloperEmail(FAKER.internet().emailAddress());
		s.setDisabledPageComponents(
			List.of(
				NavItem.builder()
					   .name(FAKER.name().name())
					   .itemText(FAKER.lorem().sentence())
					   .itemUrl(new URL("https://" + FAKER.internet().url()))
					   .build()
			)
		);
		s.setEnabledPageComponents(
			List.of(
				NavItem.builder()
					   .name(FAKER.name().name())
					   .itemText(FAKER.lorem().sentence())
					   .itemUrl(new URL("https://" + FAKER.internet().url()))
					   .build()
			)
		);
		
		return Stream.of(
			Arguments.of(s, PluginServiceSetupRequest.builder()
													 .name(FAKER.name().name())
													 .description(s.getDescription())
													 .developerName(s.getDeveloperName())
													 .developerEmail(s.getDeveloperEmail())
													 .pageComponents(
														 List.of(
															 s.getDisabledPageComponents().get(0),
															 s.getEnabledPageComponents().get(0)
														 )
													 )
													 .build()
			),
			Arguments.of(s, PluginServiceSetupRequest.builder()
													 .name(s.getName())
													 .description(FAKER.lorem().paragraph())
													 .developerName(s.getDeveloperName())
													 .developerEmail(s.getDeveloperEmail())
													 .pageComponents(
														 List.of(
															 s.getDisabledPageComponents().get(0),
															 s.getEnabledPageComponents().get(0)
														 )
													 )
													 .build()
			),
			Arguments.of(s, PluginServiceSetupRequest.builder()
													 .name(s.getName())
													 .description(s.getDescription())
													 .developerName(FAKER.name().name())
													 .developerEmail(s.getDeveloperEmail())
													 .pageComponents(
														 List.of(
															 s.getDisabledPageComponents().get(0),
															 s.getEnabledPageComponents().get(0)
														 )
													 )
													 .build()
			),
			Arguments.of(s, PluginServiceSetupRequest.builder()
													 .name(s.getName())
													 .description(s.getDescription())
													 .developerName(s.getDeveloperName())
													 .developerEmail(FAKER.internet().emailAddress())
													 .pageComponents(
														 List.of(
															 s.getDisabledPageComponents().get(0),
															 s.getEnabledPageComponents().get(0)
														 )
													 )
													 .build()
			),
			Arguments.of(s, PluginServiceSetupRequest.builder()
													 .name(s.getName())
													 .description(s.getDescription())
													 .developerName(s.getDeveloperName())
													 .developerEmail(s.getDeveloperEmail())
													 .requestedRoles(Set.of(
														 new RequestedRole(ServiceRoles.SERVICE_ROLES.get(1), "To do the thing 2", true)
													 ))
													 .pageComponents(
														 List.of(
															 s.getDisabledPageComponents().get(0),
															 s.getEnabledPageComponents().get(0)
														 )
													 )
													 .build()
			),
			Arguments.of(s, PluginServiceSetupRequest.builder()
													 .name(s.getName())
													 .description(s.getDescription())
													 .developerName(s.getDeveloperName())
													 .developerEmail(s.getDeveloperEmail())
								.pageComponents(
									List.of(
										s.getDisabledPageComponents().get(0)
									)
								)
								.build()
			)
		);
	}
	
}
