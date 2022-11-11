package tech.ebp.oqm.baseStation.interfaces.endpoints.user;

import io.quarkus.mailer.MailTemplate;
import io.quarkus.qute.Location;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;
import tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider;
import tech.ebp.oqm.baseStation.service.mongo.UserService;
import tech.ebp.oqm.baseStation.utils.EmailUtils;
import tech.ebp.oqm.lib.core.object.user.User;
import tech.ebp.oqm.lib.core.rest.auth.roles.Roles;
import tech.ebp.oqm.lib.core.rest.user.UserGetResponse;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

@Traced
@Slf4j
@Path("/api/user/utils")
@Tags({@Tag(name = "Users")})
@RequestScoped
public class UserUtils extends EndpointProvider {
	
	@Inject
	@Location("email/serverAdmin/testEmailTemplate")
	MailTemplate testMailTemplate;
	
	@Inject
	UserService userService;
	
	@Inject
	EmailUtils emailUtils;
	
	@Inject
	JsonWebToken jwt;
	
	@GET
	@Path("emailTest/self")
	@Operation(summary = "Tests that an email can be sent.")
	@APIResponse(responseCode = "200", description = "Sent the email.")
	@RolesAllowed(Roles.USER)
	public Uni<Void> sendTestEmail(@Context SecurityContext ctx) {
		logRequestContext(this.jwt, ctx);
		
		User user = this.userService.getFromJwt(this.jwt);
		
		return this.emailUtils
				   .setupDefaultEmailData(
					   testMailTemplate,
					   UserGetResponse.builder(user).build(),
					   "Test Email"
				   )
				   .send();
	}
	
	@GET
	@Path("emailTest/{userId}")
	@Operation(summary = "Tests that an email can be sent.")
	@APIResponse(responseCode = "200", description = "Sent the email.")
	@RolesAllowed(Roles.USER_ADMIN)
	public  Uni<Void> sendTestEmail(
		@Context SecurityContext ctx,
		@PathParam("userId") String userId
	) {
		logRequestContext(this.jwt, ctx);
		
		User adminUser = this.userService.getFromJwt(this.jwt);
		User userTo = this.userService.get(userId);
		
		return this.emailUtils
				   .setupDefaultEmailData(
					   testMailTemplate,
					   UserGetResponse.builder(userTo).build(),
					   "Test Email"
				   )
				   .cc(adminUser.getEmail())
				   .send();
	}
}
