//package tech.ebp.oqm.baseStation.interfaces.endpoints.user;
//
//import io.opentelemetry.instrumentation.annotations.WithSpan;
//import io.quarkus.mailer.MailTemplate;
//import io.quarkus.qute.Location;
//import io.smallrye.mutiny.Uni;
//import lombok.extern.slf4j.Slf4j;
//import org.eclipse.microprofile.jwt.JsonWebToken;
//import org.eclipse.microprofile.openapi.annotations.Operation;
//import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
//import org.eclipse.microprofile.openapi.annotations.tags.Tag;
//import org.eclipse.microprofile.openapi.annotations.tags.Tags;
//import tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider;
//import tech.ebp.oqm.baseStation.service.mongo.UserService;
//import tech.ebp.oqm.baseStation.utils.EmailUtils;
//import tech.ebp.oqm.baseStation.model.object.interactingEntity.user.User;
//import tech.ebp.oqm.baseStation.model.rest.auth.roles.Roles;
//import tech.ebp.oqm.baseStation.model.rest.user.UserGetResponse;
//import tech.ebp.oqm.baseStation.model.rest.user.availability.EmailAvailabilityResponse;
//import tech.ebp.oqm.baseStation.model.rest.user.availability.UsernameAvailabilityResponse;
//
//import jakarta.annotation.security.PermitAll;
//import jakarta.annotation.security.RolesAllowed;
//import jakarta.enterprise.context.RequestScoped;
//import jakarta.inject.Inject;
//import jakarta.validation.constraints.Email;
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.NotNull;
//import jakarta.validation.constraints.Size;
//import jakarta.ws.rs.GET;
//import jakarta.ws.rs.Path;
//import jakarta.ws.rs.PathParam;
//import jakarta.ws.rs.Produces;
//import jakarta.ws.rs.core.Context;
//import jakarta.ws.rs.core.MediaType;
//import jakarta.ws.rs.core.SecurityContext;
//
//import static tech.ebp.oqm.baseStation.interfaces.endpoints.EndpointProvider.ROOT_API_ENDPOINT_V1;
//
//@Slf4j
//@Path(ROOT_API_ENDPOINT_V1 + "/user/utils")
//@Tags({@Tag(name = "Users")})
//@RequestScoped
//public class UserUtils extends EndpointProvider {
//
//	@Inject
//	@Location("email/serverAdmin/testEmailTemplate")
//	MailTemplate testMailTemplate;
//
//	@Inject
//	EmailUtils emailUtils;
//
//	@Inject
//	JsonWebToken jwt;
//
//	@GET
//	@Path("emailTest/self")
//	@Operation(summary = "Tests that an email can be sent.")
//	@APIResponse(responseCode = "200", description = "Sent the email.")
//	@RolesAllowed(Roles.USER)
//	public Uni<Void> sendTestEmail(@Context SecurityContext ctx) {
//
//		User user = this.userService.getFromJwt(this.jwt);
//
//		return this.emailUtils
//				   .setupDefaultEmailData(
//					   testMailTemplate,
//					   UserGetResponse.builder(user).build(),
//					   "Test Email"
//				   )
//				   .send();
//	}
//
//	@GET
//	@Path("emailTest/{userId}")
//	@Operation(summary = "Tests that an email can be sent.")
//	@APIResponse(responseCode = "200", description = "Sent the email.")
//	@RolesAllowed(Roles.USER_ADMIN)
//	public Uni<Void> sendTestEmail(
//		@Context SecurityContext ctx,
//		@PathParam("userId") String userId
//	) {
//		logRequestContext(this.jwt, ctx);
//
//		User adminUser = this.userService.getFromJwt(this.jwt);
//		User userTo = this.userService.get(userId);
//
//		return this.emailUtils
//				   .setupDefaultEmailData(
//					   testMailTemplate,
//					   UserGetResponse.builder(userTo).build(),
//					   "Test Email"
//				   )
//				   .cc(adminUser.getEmail())
//				   .send();
//	}
//
//}
