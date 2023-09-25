package tech.ebp.oqm.baseStation.model.object.interactingEntity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import tech.ebp.oqm.baseStation.model.object.AttKeywordMainObject;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.externalService.GeneralService;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.externalService.plugin.PluginService;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.user.User;
import tech.ebp.oqm.baseStation.service.JwtUtils;

import java.util.Set;

@Slf4j
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "interactingEntityType"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = User.class, name = "USER"),
	@JsonSubTypes.Type(value = PluginService.class, name = "SERVICE_PLUGIN"),
	@JsonSubTypes.Type(value = GeneralService.class, name = "SERVICE_GENERAL"),
})
@BsonDiscriminator
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class InteractingEntity extends AttKeywordMainObject {
	
	@NonNull
	@NotNull
	@NotBlank
	private String idFromAuthProvider;
	@NonNull
	@NotNull
	@NotBlank
	private String authProvider;
	
	public abstract String getName();
	
	public abstract String getEmail();
	
	public abstract InteractingEntityType getInteractingEntityType();
	
	public abstract Set<String> getRoles();
	
	public abstract boolean updateFrom(JsonWebToken jwt);
	
	public static InteractingEntity createEntity(JsonWebToken jwt){
		InteractingEntity newEntity;
		
		//TODO:: support services better. Probably should setup keycloak to set some of these values.
		if(((String)jwt.getClaim(Claims.upn)).startsWith("service-account-")){
			GeneralService newService = new GeneralService();
			
			newService.setName(jwt.getClaim(Claims.upn));
			newService.setDescription("Service account from OIDC provider.");
			newService.setDeveloperEmail("foo@bar.com");
			newService.setDeveloperName("Developers");
			
			newEntity = newService;
		} else {
			User newUser = new User();
			newEntity = newUser;
			newUser.setEmail(JwtUtils.getEmail(jwt));
			newUser.setName(JwtUtils.getName(jwt));
			newUser.setUsername(JwtUtils.getUserName(jwt));
			newUser.setRoles(JwtUtils.getRoles(jwt));
		}
		newEntity.setAuthProvider(jwt.getIssuer());
		newEntity.setIdFromAuthProvider(jwt.getSubject());
		
		log.debug("New entity: {}", newEntity);
		return newEntity;
	}
	
}
