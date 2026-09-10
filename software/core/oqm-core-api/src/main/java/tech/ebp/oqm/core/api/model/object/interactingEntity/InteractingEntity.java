package tech.ebp.oqm.core.api.model.object.interactingEntity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.SecurityContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import tech.ebp.oqm.core.api.config.CoreApiInteractingEntity;
import tech.ebp.oqm.core.api.model.object.AttKeywordMainObject;
import tech.ebp.oqm.core.api.model.object.interactingEntity.externalService.GeneralService;
import tech.ebp.oqm.core.api.model.object.interactingEntity.user.User;
import tech.ebp.oqm.core.api.model.validation.annotations.ValidServiceRole;
import tech.ebp.oqm.core.api.service.JwtUtils;

import java.util.HashSet;
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
@Schema(oneOf = {User.class, GeneralService.class, CoreApiInteractingEntity.class})
public abstract class InteractingEntity extends AttKeywordMainObject {

	public static final int CUR_SCHEMA_VERSION = 2;

	/**
	 * The ID provided by the authentication provider. Used as the main ID to identify the entity when requests come in.
	 */
	@Schema(description = "The id of the entity from the auth provider. This is used to link the user as kept track of here to the auth provider.")
	private String idFromAuthProvider;

	/**
	 * The name of the auth provider that this entity used to authenticate.
	 */
	private String authProvider;

	/**
	 * The name of the entity.
	 * @return The name of the entity
	 */
	@NotNull
	public abstract String getName();

	/**
	 * The email that can be used to contact the entity
	 * @return The email that can be used to contact the entity
	 */
	@Nullable
	public abstract String getEmail();

	/**
	 * The type of this entity
	 * @return The type of this entity
	 */
	public abstract InteractingEntityType getType();

	/**
	 * The roles this entity has to interact with the system.
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private Set<@ValidServiceRole String> roles = new HashSet<>();

	/**
	 * A function called to update this entity from a JWT.
	 * @param jwt The JWT to update information from.
	 * @return If this object was updated or not
	 */
	public abstract boolean updateFrom(JsonWebToken jwt);

	/**
	 * Creates an entity
	 * @param jwt
	 * @return
	 */
	public static InteractingEntity createEntity(JsonWebToken jwt) {
		InteractingEntity newEntity;

		if (((String) jwt.getClaim(Claims.upn)).startsWith("service-account-")) {
			GeneralService newService = new GeneralService();

			newService.setName(JwtUtils.getServiceName(jwt));
			newService.setDeveloperEmail(JwtUtils.getDevEmail(jwt));
			newService.setDeveloperName(JwtUtils.getDevName(jwt));
			newService.setDeveloperWebsite(JwtUtils.getDevWebsite(jwt));

			newEntity = newService;
		} else {
			User newUser = new User();

			newUser.setName(JwtUtils.getName(jwt));
			newUser.setEmail(JwtUtils.getEmail(jwt));
			newUser.setUsername(JwtUtils.getUserName(jwt));

			newEntity = newUser;
		}
		newEntity.setRoles(JwtUtils.getRoles(jwt));
		newEntity.setAuthProvider(jwt.getIssuer());
		newEntity.setIdFromAuthProvider(jwt.getSubject());

		log.debug("New entity from jwt: {}", newEntity);
		return newEntity;
	}

	public static InteractingEntity createEntity(SecurityContext context) {
		User newUser = new User();
		newUser.setName(context.getUserPrincipal().getName());

		log.debug("New entity from security context: {}", newUser);
		return newUser;
	}

	@Override
	public int getSchemaVersion() {
		return CUR_SCHEMA_VERSION;
	}
}
