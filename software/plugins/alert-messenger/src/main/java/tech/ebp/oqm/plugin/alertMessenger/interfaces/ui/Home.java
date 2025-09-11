package tech.ebp.oqm.plugin.alertMessenger.interfaces.ui;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.alertMessenger.repositories.UserPreferencesRepository;
import tech.ebp.oqm.plugin.alertMessenger.repositories.UserRepository;
import tech.ebp.oqm.plugin.alertMessenger.AlertConsumer;
import tech.ebp.oqm.plugin.alertMessenger.model.UserInfo;
import tech.ebp.oqm.plugin.alertMessenger.model.UserPreferences;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/*
 * Remove this after testing
 */
import org.eclipse.microprofile.reactive.messaging.Message;

@Slf4j
@Path("/")
@Tags({ @Tag(name = "UI") })
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class Home extends UiInterface {

	@Getter
	@Inject
	@Location("webui/pages/index")
	Template pageTemplate;

	@Inject
	JsonWebToken jwt; // Extracts claims from the JWT

	@Inject
	UserRepository userRepository; // Repository for database operations

	@Inject
	UserPreferencesRepository userPreferencesRepository;


	@Inject
	SecurityIdentity identity; // Provides user and role info


	// Ensures the user is saved/updated in the database based on JWT claims
	// and renders the home page with the user's information.
	@GET
	@RolesAllowed("inventoryView")
	@Produces(MediaType.TEXT_HTML)
	@Transactional // Ensures a transaction is active
	public Response index() {
		log.info("Got index page");

		// Extract user details from the JWT
		String idString = jwt.getClaim("sub"); // Extract the ID as a string
		UUID id;
		try {
			id = UUID.fromString(idString); // Convert String to UUID
		} catch (IllegalArgumentException e) {
			log.error("Invalid UUID format in 'sub' claim: {}", idString);
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid user ID format").build();
		}

		String name = jwt.getClaim("name");
		String username = jwt.getClaim("preferred_username");
		String email = jwt.getClaim("email");
		Set<String> roles = identity.getRoles(); // Retrieves user roles

		// Ensure the user is saved or updated in the database
		UserInfo existingUser = userRepository.findById(id);
		if (existingUser != null) {
			existingUser.setName(name);
			existingUser.setUsername(username);
			existingUser.setEmail(email);
			existingUser.getRoles().clear(); // Clear existing roles
			existingUser.getRoles().addAll(roles); // Add new roles
			// No need to merge; Hibernate automatically tracks changes to managed entities.
		} else {
			log.info("New user detected, saving to database: {}", username);
			UserInfo newUser = UserInfo.builder()
					.id(id)
					.name(name)
					.username(username)
					.email(email)
					.roles(roles)
					.build();
			userRepository.persist(newUser);
		}

		// Render the UI page

		return Response.ok(
				this.setupPageTemplate(this.pageTemplate)).build();
	}

	// Updates user notification preferences (email and Slack webhook) in the database.
	@POST
	@Path("/updatePreferences")
	@PermitAll
	@Transactional
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response updatePreferences(@FormParam("id") String idString,
									  @FormParam("emailNotifications") String emailNotifications,
									  @FormParam("slackWebhook") String slackWebhook) {
	
		log.info("Received data: id={}, emailNotifications={}, slackWebhook={}", idString, emailNotifications, slackWebhook);
	
		UUID id;
		try {
			id = UUID.fromString(idString);
		} catch (IllegalArgumentException e) {
			log.error("Invalid UUID format for ID: {}", idString);
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid ID format").build();
		}
	
		// Retrieve or create the preferences record
		UserPreferences preferences = userPreferencesRepository.findByIdOptional(id).orElseGet(() -> {
			log.info("Creating new preferences record for user: {}", id);
			UserPreferences newPreferences = new UserPreferences();
			newPreferences.setId(id);
			userPreferencesRepository.persist(newPreferences);
			return newPreferences;
		});
	
		// Update preferences
		preferences.setEmailNotifications(emailNotifications);
		preferences.setSlackWebhook(slackWebhook);
	
		// Persist updated preferences
		userPreferencesRepository.persist(preferences);
	
		return Response.ok("Preferences updated successfully!").build();
	}
}
