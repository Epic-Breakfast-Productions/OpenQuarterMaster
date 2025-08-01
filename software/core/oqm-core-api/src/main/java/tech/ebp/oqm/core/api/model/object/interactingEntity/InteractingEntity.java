package tech.ebp.oqm.core.api.model.object.interactingEntity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.ws.rs.core.SecurityContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import tech.ebp.oqm.core.api.config.CoreApiInteractingEntity;
import tech.ebp.oqm.core.api.model.object.AttKeywordMainObject;
import tech.ebp.oqm.core.api.model.object.interactingEntity.externalService.GeneralService;
import tech.ebp.oqm.core.api.model.object.interactingEntity.externalService.plugin.PluginService;
import tech.ebp.oqm.core.api.model.object.interactingEntity.user.User;
import tech.ebp.oqm.core.api.service.JwtUtils;

import java.util.Set;

/**
 * Class responsible for describing entities that interact with this system.
 * <p>
 * TODO:: make tolerant to not all fields being specified for users; #868
 */
@Slf4j
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = User.class, name = "USER"),
	@JsonSubTypes.Type(value = PluginService.class, name = "SERVICE_PLUGIN"),
	@JsonSubTypes.Type(value = GeneralService.class, name = "SERVICE_GENERAL"),
	@JsonSubTypes.Type(value = CoreApiInteractingEntity.class, name = "CORE_API"),
})
@BsonDiscriminator
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
public abstract class InteractingEntity extends AttKeywordMainObject {
	
	public static final int CUR_SCHEMA_VERSION = 2;
	
	private String idFromAuthProvider;
	private String authProvider;
	
	public abstract String getName();
	
	public abstract String getEmail();
	
	public abstract InteractingEntityType getType();
	
	public abstract Set<String> getRoles();
	
	public abstract boolean updateFrom(JsonWebToken jwt);
	
	public static InteractingEntity createEntity(JsonWebToken jwt) {
		InteractingEntity newEntity;
		
		//TODO:: support services better. Probably should setup keycloak to set some of these values.
		if (((String) jwt.getClaim(Claims.upn)).startsWith("service-account-")) {
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
	
	public static InteractingEntity createEntity(SecurityContext context) {
		User newUser = new User();
		newUser.setName(context.getUserPrincipal().getName());
		
		log.debug("New entity: {}", newUser);
		return newUser;
	}
	
	@Override
	public int getSchemaVersion() {
		return CUR_SCHEMA_VERSION;
	}
}
