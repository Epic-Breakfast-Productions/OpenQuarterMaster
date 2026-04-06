package tech.ebp.oqm.core.baseStation.interfaces;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.opentelemetry.api.trace.Span;
import io.quarkus.oidc.IdToken;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniJoin;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.core.baseStation.interfaces.ui.pages.UiProvider;
import tech.ebp.oqm.core.baseStation.model.UserInfo;
import tech.ebp.oqm.core.baseStation.utils.JwtUtils;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.dataHelpers.OqmDatabaseService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;

import java.util.Optional;

@Slf4j
@NoArgsConstructor
public abstract class RestInterface {

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

	@Getter
	@HeaderParam("x-forwarded-prefix")
	Optional<String> forwardedPrefix;
	
	@Getter
	@RestClient
	OqmCoreApiClientService oqmCoreApiClient;

	protected String getRootPrefix(){
		return this.forwardedPrefix.orElse("");
	}

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

	private void logRequestAndProcessEntity() {
		this.userInfo = JwtUtils.getUserInfo(this.getUserToken());
		log.info(
			"Processing request with JWT; User:{} ssh:{} jwtIssuer: {} roles: {}",
			this.userInfo.getName(),
			this.getSecurityContext().isSecure(),
			this.idToken.getIssuer(),
			this.idToken.getGroups()
		);
		if (this.getSecurityContext().isSecure()) {
			log.warn("Request with JWT made without HTTPS");
		}
		log.debug("User JWT: {}", this.getBearerHeaderStr());
	}

	@PostConstruct
	void initialLogAndEntityProcess() {
		this.oqmDatabases = this.oqmDatabaseService.getDatabases();
		this.logRequestAndProcessEntity();
	}

	protected Uni<ObjectNode> addParentLabelsToSearchResults(ObjectNode results, String labelKey, UiProvider.ObjGetMethod parentGetCall) {
		UniJoin.Builder<ObjectNode> uniJoinBuilder = Uni.join().builder();

		//TODO:: do map thing to not call more than needed
		boolean hadParents = false;
		for (JsonNode curResult : (ArrayNode) results.get("results")) {
			if (curResult.get("hasParent").asBoolean()) {
				hadParents = true;
				uniJoinBuilder.add(
					parentGetCall.get(getBearerHeaderStr(), getSelectedDb(), curResult.get("parent").asText())
						.invoke((ObjectNode storageBlock) -> {
							((ObjectNode) curResult).set("parentLabel", storageBlock.get(labelKey));
						})
				);
			}
		}
		if (!hadParents) {
			return Uni.createFrom().item(results);
		}
		return uniJoinBuilder.joinAll()
			.andCollectFailures()
			.map((list) -> {
				return results;
			});
	}

	public String getSelectedDb() {
		if (this.oqmDb == null || this.oqmDb.isBlank()) {
			if(this.oqmDatabases == null || this.oqmDatabases.isEmpty()){
				throw new IllegalStateException("Cannot have no databases.");
			}
			//TODO: this but smarter?
			return this.getOqmDatabases().get(0).get("id").asText();
		}
		return this.oqmDb;
	}
}
