package tech.ebp.oqm.baseStation.interfaces.endpoints.user;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.baseStation.testResources.data.TestUserService;
import tech.ebp.oqm.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.user.User;

import javax.inject.Inject;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static tech.ebp.oqm.baseStation.testResources.TestRestUtils.setupJwtCall;

@Slf4j
@Tag("integration")
@QuarkusTest
@QuarkusTestResource(value = TestResourceLifecycleManager.class)
@TestHTTPEndpoint(UserUtils.class)
class UserUtilsTest {
	
	
	@Inject
	MockMailbox mockMailbox;
	
	@Inject
	TestUserService testUserService;
	
	private void assertProperTestEmail(Mail email, User userTo){
		//TODO
	}
	
	@Test
	public void testSendTestEmail() {
		User user = this.testUserService.getTestUser(false, true);
		
		ValidatableResponse response = setupJwtCall(given(), this.testUserService.getTestUserToken(user))
										   .get("emailTest/self")
										   .then();
		
		response.statusCode(HttpStatus.SC_NO_CONTENT);
		
		List<Mail> mailList = mockMailbox.getMessagesSentTo(user.getEmail());
		assertEquals(1, mailList.size());
		assertProperTestEmail(mailList.get(0), user);
	}
	
	@Test
	public void testSendTestEmailToOtherUser() {
		User userAdmin = this.testUserService.getTestUser(true, true);
		User user = this.testUserService.getTestUser(false, true);
		
		ValidatableResponse response = setupJwtCall(given(), this.testUserService.getTestUserToken(userAdmin))
										   .get("emailTest/" + user.getId())
										   .then();
		
		response.statusCode(HttpStatus.SC_NO_CONTENT);
		
		List<Mail> mailList = mockMailbox.getMessagesSentTo(user.getEmail());
		assertEquals(1, mailList.size());
		assertProperTestEmail(mailList.get(0), user);
		
		mailList = mockMailbox.getMessagesSentTo(userAdmin.getEmail());
		assertEquals(1, mailList.size());
		assertProperTestEmail(mailList.get(0), user);
		
		assertEquals(1, mailList.get(0).getCc().size());
		assertEquals(userAdmin.getEmail(), mailList.get(0).getCc().get(0));
		
	}
	
}