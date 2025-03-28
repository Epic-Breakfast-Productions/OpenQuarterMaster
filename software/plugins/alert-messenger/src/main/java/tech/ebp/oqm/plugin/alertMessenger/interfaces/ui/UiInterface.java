package tech.ebp.oqm.plugin.alertMessenger.interfaces.ui;

import com.fasterxml.jackson.databind.node.ArrayNode;
import io.quarkus.oidc.IdToken;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.OqmDatabaseService;
import tech.ebp.oqm.plugin.alertMessenger.model.UserInfo;
import tech.ebp.oqm.plugin.alertMessenger.utils.JwtUtils;

@Slf4j
public abstract class UiInterface {

	/**
	 * For normal use, don't use this. use the `oqmDatabases`
	 */
	@Inject
	@Getter(AccessLevel.PROTECTED)
	OqmDatabaseService oqmDatabaseService;

	@Getter(AccessLevel.PROTECTED)
	@Inject
	@IdToken
	JsonWebToken idToken;

	@Getter(AccessLevel.PROTECTED)
	@Inject
	JsonWebToken accessToken;

	@Getter(AccessLevel.PROTECTED)
	@Context
	SecurityContext securityContext;

	@Getter(AccessLevel.PROTECTED)
	UserInfo userInfo;

	@CookieParam("oqmDb")
	String oqmDb;

	@Getter(AccessLevel.PROTECTED)
	ArrayNode oqmDatabases;

	protected boolean hasIdToken() {
		return this.getIdToken() != null &&
			this.getIdToken()
				.getClaimNames() != null;
	}

	protected boolean hasAccessToken() {
		return this.getAccessToken() != null && this.getAccessToken().getClaimNames() != null;
	}

	/**
	 * When hit from bare API call with just bearer, token will be access token.
	 * <p>
	 * When hit from ui, idToken.
	 *
	 * @return
	 */
	protected JsonWebToken getUserToken() {
		if (this.hasIdToken()) {
			log.debug("Had id token");
			return this.getIdToken();
		}
		if (this.hasAccessToken()) {
			log.debug("Had access token");
			return this.getAccessToken();
		}
		return null;
	}

	protected String getUserTokenStr() {
		return this.getAccessToken().getRawToken();
	}

	protected String getBearerHeaderStr() {
		return "Bearer " + this.getUserTokenStr();
	}

	@PostConstruct
	void initialLogAndEntityProcess() {
		if (this.getUserToken() == null) {
			log.warn("JWT token is null. Skipping user initialization.");
			return;
		}

		this.oqmDatabases = this.oqmDatabaseService.getDatabases();
		this.userInfo = JwtUtils.getUserInfo(this.getUserToken());

		log.info("User roles: {}", this.userInfo.getRoles());
	}

	public String getSelectedDb() {
		if (this.oqmDb == null || this.oqmDb.isBlank()) {
			if(this.oqmDatabases == null || this.oqmDatabases.isEmpty()){
				throw new IllegalStateException("Cannot have no databases.");
			}
			return this.getOqmDatabases().get(0).get("id").asText();
		}
		return this.oqmDb;
	}

	protected TemplateInstance setupPageTemplate(Template template) {
		return template
			.data("userInfo", this.getUserInfo())
			.data("oqmDbs", this.getOqmDatabases())
			.data("selectedOqmDb", this.getSelectedDb())
			;
	}
}
