package tech.ebp.oqm.plugin.alertMessenger.interfaces.ui;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.alertMessenger.repositories.UserRepository;
import tech.ebp.oqm.plugin.alertMessenger.model.UserInfo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import java.util.Set;
import java.util.UUID;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

@Slf4j
@Path("/")
@Tags({@Tag(name = "UI")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class Home extends UiInterface {

	@Getter
	@Inject
	@Location("webui/pages/index")
	Template pageTemplate;

	// EVERYTHING BELOW THIS HAS JUST BEEN ADDED BY CHATGPT
	@Inject
    JsonWebToken jwt; // Extracts claims from the JWT

    @Inject
    UserRepository userRepository; // Repository for database operations

    @Inject
    SecurityIdentity identity; // Provides user and role info

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
        UserInfo existingUser = userRepository.findById(id); // Find by unique ID
        if (existingUser == null) {
			log.info("New user detected, saving to database: {}", username);
			UserInfo newUser = UserInfo.builder()
					.id(id)
					.name(name)
					.username(username)
					.email(email)
					.roles(roles)
					.build();
			userRepository.persist(newUser); // Save new user
		} else {
			log.info("Existing user found: {}, updating details", username);
			existingUser.setName(name);
			existingUser.setUsername(username);
			existingUser.setEmail(email);
			existingUser.setRoles(roles);
			userRepository.merge(existingUser); // Merge the detached entity
		}		

        // Render the UI page

		return Response.ok(
			this.setupPageTemplate(this.pageTemplate)
		).build();
	}

}
