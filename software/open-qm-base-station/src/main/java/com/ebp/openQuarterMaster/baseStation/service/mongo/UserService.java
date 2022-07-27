package com.ebp.openQuarterMaster.baseStation.service.mongo;

import com.ebp.openQuarterMaster.baseStation.service.JwtService;
import com.ebp.openQuarterMaster.baseStation.utils.AuthMode;
import com.ebp.openQuarterMaster.lib.core.rest.user.UserLoginRequest;
import com.ebp.openQuarterMaster.lib.core.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.opentracing.Traced;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ebp.openQuarterMaster.baseStation.service.JwtService.JWT_USER_TITLE_CLAIM;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.or;

@Traced
@Slf4j
@ApplicationScoped
public class UserService extends MongoHistoriedService<User> {
	
	private Validator validator;
	private AuthMode authMode;
	
	UserService() {//required for DI
		super(null, null, null, null, null, null, false, null);
	}
	
	@Inject
	UserService(
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
	
	/**
	 * Gets a user from either their username or email.
	 *
	 * @param usernameOrEmail The username or email of the user.
	 *
	 * @return
	 */
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
	
	public User getFromLoginRequest(UserLoginRequest loginRequest) {
		return this.getFromUsernameEmail(loginRequest.getUsernameEmail());
	}
	
	private User getExternalUser(String externalSource, String externalId) {
		if (externalId == null) {
			return null;
		}
		return this.getCollection().find(eq("externIds." + externalSource, externalId)).limit(1).first();
	}
	
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
		User.Builder userBuilder = User.builder()
									   .firstName(jwt.getClaim(Claims.given_name))
									   .lastName(jwt.getClaim(Claims.family_name))
									   .email(jwt.getClaim(Claims.email))
									   .title(jwt.getClaim(JWT_USER_TITLE_CLAIM))
									   .roles(jwt.getClaim(Claims.groups));
		
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
	public User getFromJwt(JsonWebToken jwt) {
		
		switch (this.authMode) {
			case SELF:
				log.debug("Getting user data from self.");
				String userId = jwt.getClaim(JwtService.JWT_USER_ID_CLAIM);
				return this.get(userId);
			case EXTERNAL:
				log.debug("Getting external user data ");
				return this.getOrCreateExternalUser(jwt);
		}
		return null;
	}
}
