package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.security.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.user.User;
import tech.ebp.oqm.baseStation.model.rest.auth.roles.Roles;
import tech.ebp.oqm.baseStation.model.rest.auth.roles.UserRoles;
import tech.ebp.oqm.baseStation.model.rest.auth.user.UserLoginRequest;
import tech.ebp.oqm.baseStation.rest.search.UserSearch;
import tech.ebp.oqm.baseStation.service.JwtService;
import tech.ebp.oqm.baseStation.service.mongo.exception.DbNotFoundException;
import tech.ebp.oqm.baseStation.utils.AuthMode;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Filters.or;
import static tech.ebp.oqm.baseStation.service.JwtService.JWT_USER_TITLE_CLAIM;

@Slf4j
@ApplicationScoped
public class UserService extends MongoHistoriedObjectService<User, UserSearch> {
	
	private Validator validator;
	private AuthMode authMode;
	
	UserService() {//required for DI
		super(null, null, null, null, null, null, true, null);
	}
	
	@Inject
	public UserService(
		Validator validator,
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		@ConfigProperty(name = "quarkus.mongodb.database")
		String database,
		@ConfigProperty(name = "service.authMode")
		AuthMode authMode
	) {
		super(
			objectMapper,
			mongoClient,
			database,
			User.class,
			true
		);
		this.validator = validator;
		this.authMode = authMode;
	}
	
	@WithSpan
	@Override
	public void ensureObjectValid(boolean newObject, User newOrChangedObject, ClientSession clientSession) {
		super.ensureObjectValid(newObject, newOrChangedObject, clientSession);
		//TODO:: username/email not existant
		
		if(this.authMode == AuthMode.SELF) {
			if (newOrChangedObject.isDisabled()) {
				List<User> adminUsers = this.list(//TODO:: client session
					and(
						eq("disabled", false),
						in("roles", Roles.USER_ADMIN)
					),
					null,
					null
				);
				
				if (adminUsers.size() == 1) {
					if (adminUsers.get(0).getId().equals(newOrChangedObject.getId())) {
						if (
							newOrChangedObject.isDisabled() ||
							!newOrChangedObject.getRoles().contains(Roles.USER_ADMIN)
						) {
							throw new IllegalArgumentException("Must always have at least one enabled user admin.");
						}
					}
				}
			}
		}
	}
	
	/**
	 * Gets a user from either their username or email.
	 *
	 * @param usernameOrEmail The username or email of the user.
	 *
	 * @return
	 */
	@WithSpan
	public User getFromUsernameEmail(String usernameOrEmail) {
		return this.getCollection()
				   .find(
					   or(
						   eq("email", usernameOrEmail),
						   eq("username", usernameOrEmail)
					   )
				   )
				   .limit(1)
				   .first();
	}
	
	@WithSpan
	public boolean usernameAvailable(
		@NotNull
		@NotBlank
		@Size(max = 30)
		String username
	){
		return !this.fieldValueExists("username", username);
	}
	
	@WithSpan
	public boolean emailAvailable(
		@Email
		String email
	){
		return !this.fieldValueExists("email", email);
	}
	
	@WithSpan
	public User getFromLoginRequest(UserLoginRequest loginRequest) {
		return this.getFromUsernameEmail(loginRequest.getUsernameEmail());
	}
	
	@WithSpan
	private User getExternalUser(String externalSource, String externalId) {
		if (externalId == null) {
			return null;
		}
		return this.getCollection().find(eq("externIds." + externalSource, externalId)).limit(1).first();
	}
	
	@WithSpan
	private User getOrCreateExternalUser(JsonWebToken jwt) {
		String externalSource = jwt.getIssuer();
		String externalId = jwt.getClaim(Claims.sub);
		log.debug("User id from external jwt: {}", externalId);
		User user = this.getExternalUser(externalSource, externalId);
		
		if (user != null) {
			//TODO:: update from given jwt, if needed
			return user;
		}
		
		//TODO:: verify has all these fields & throw exception
		
		
		Set<String> userRoles = jwt.getClaim(Claims.groups);
		userRoles.retainAll(UserRoles.USER_ROLES);
		
		User.Builder userBuilder = User.builder()
									   .firstName(jwt.getClaim(Claims.given_name))
									   .lastName(jwt.getClaim(Claims.family_name))
									   .email(jwt.getClaim(Claims.email))
									   .title(jwt.getClaim(JWT_USER_TITLE_CLAIM))
									   .roles(userRoles);
		
		if (jwt.getClaim(Claims.upn) != null) {
			userBuilder.username(jwt.getClaim(Claims.upn));
		} else if (jwt.getClaim(Claims.preferred_username) != null) {
			userBuilder.username(jwt.getClaim(Claims.preferred_username));
		}
		
		userBuilder.externIds(new HashMap<>() {{
			put(externalSource, externalId);
		}});
		
		user = userBuilder.build();
		
		Set<ConstraintViolation<User>> validationErrs = this.validator.validate(user);
		if (!validationErrs.isEmpty()) {
			throw new IllegalStateException(
				"Resulting user from jwt wasn't valid: " +
				validationErrs.stream().map(ConstraintViolation<User>::getMessage).collect(Collectors.joining(", "))
			);
		}
		//TODO:: check if username or email already exists
		
		this.add(user, null);
		return user;
	}
	
	/**
	 * Gets a user from the given jwt.
	 * <p>
	 * If {@link #authMode} is set to {@link AuthMode#SELF}, simple lookup.
	 * <p>
	 * If {@link #authMode} is set to {@link AuthMode#EXTERNAL}, the service will lookup based on the external id in the jwt, creating the
	 * user if they don't exist yet.
	 *
	 * @param jwt The jwt to get the user for
	 *
	 * @return The user the jwt was for. Null if no user found.
	 */
	@WithSpan
	public User getFromJwt(JsonWebToken jwt) {
		//TODO:: check is user?
		switch (this.authMode) {
			case SELF:
				log.debug("Getting user data from self.");
				String userId = jwt.getClaim(JwtService.JWT_USER_ID_CLAIM);
				if(userId == null){
					return null;
				}
				try {
					return this.get(userId);
				} catch(DbNotFoundException e){
					throw new UnauthorizedException("User in JWT not found.");
				}
			case EXTERNAL:
				log.debug("Getting external user data ");
				return this.getOrCreateExternalUser(jwt);
		}
		return null;
	}
}