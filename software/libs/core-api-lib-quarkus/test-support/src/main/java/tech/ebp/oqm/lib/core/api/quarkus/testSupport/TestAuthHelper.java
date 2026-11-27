package tech.ebp.oqm.lib.core.api.quarkus.testSupport;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;
import io.quarkus.scheduler.Scheduler;
import io.restassured.http.Header;
import io.vertx.ext.auth.impl.jose.JWT;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.Constants;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.sso.KeycloakRestClient;

import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.locks.ReentrantLock;

import static io.restassured.RestAssured.*;
import static tech.ebp.oqm.lib.core.api.quarkus.runtime.sso.KcClientAuthService.SERVICE_ACCOUNT_TOKEN_REFRESH_JOB_NAME;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestAuthHelper extends CoreApiLibClientHelper {

	private static String authString;
	private static Instant expires;

	private static String getAuthUri() {
		return ConfigProvider.getConfig().getValue("quarkus.rest-client.\"" + Constants.CORE_API_CLIENT_OIDC_NAME + "\".url", String.class) + "/protocol/openid-connect/token";
	}

	private static String getNewClientAuthHeaderString() {
		String clientId = ConfigProvider.getConfig().getValue("quarkus.oidc.client-id", String.class);
		String clientSecret = ConfigProvider.getConfig().getValue("quarkus.oidc.credentials.secret", String.class);

		return "Basic: " + Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
	}

	public static synchronized String getAuthString() {
		if(authString == null || expires.isBefore(Instant.now())) {
			ObjectNode tokenResponse = given()
				.when()
				.header(new Header("Authorization", getNewClientAuthHeaderString()))
				.contentType(ContentType.APPLICATION_FORM_URLENCODED.getMimeType())
				.formParam("grant_type", "client_credentials")
				.post(getAuthUri())
				.then()
				.statusCode(200)
				.extract().as(ObjectNode.class);
			authString = "Bearer " + tokenResponse.get("access_token").asText();
			expires = Instant.now().plusSeconds((tokenResponse.get("expires_in").asInt() / 4) * 3);
		}
		return authString;
	}
}
