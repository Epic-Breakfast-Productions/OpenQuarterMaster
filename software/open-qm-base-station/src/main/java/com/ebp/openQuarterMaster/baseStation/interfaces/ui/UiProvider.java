package com.ebp.openQuarterMaster.baseStation.interfaces.ui;

import com.ebp.openQuarterMaster.baseStation.restCalls.KeycloakServiceCaller;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.PagingCalculations;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.PagingOptions;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SearchResult;
import com.ebp.openQuarterMaster.baseStation.utils.AuthMode;
import com.ebp.openQuarterMaster.lib.core.rest.user.UserGetResponse;
import com.fasterxml.jackson.databind.JsonNode;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.ws.rs.core.SecurityContext;
import java.time.ZonedDateTime;

import static com.ebp.openQuarterMaster.baseStation.utils.AuthMode.EXTERNAL;

@Slf4j
public abstract class UiProvider {
	
	protected static final String USER_INFO_DATA_KEY = "userInfo";
	
	protected static void logRequestContext(JsonWebToken jwt, SecurityContext context) {
		if (!hasJwt(jwt)) {
			log.info("Processing request with no JWT; ssh:{}", context.isSecure());
		} else {
			log.info(
				"Processing request with JWT; User:{} ssh:{} jwtIssuer: {}",
				context.getUserPrincipal().getName(),
				context.isSecure(),
				jwt.getIssuer()
			);
			if (context.isSecure()) {
				log.warn("Request with JWT made without HTTPS");
			}
		}
		log.debug("Raw jwt: {}", jwt.getRawToken());
	}
	
	protected static boolean hasJwt(JsonWebToken jwt) {
		return jwt != null && jwt.getClaimNames() != null;
	}
	
	protected static JsonNode refreshAuthToken(KeycloakServiceCaller ksc, String refreshCode) {
		if (!EXTERNAL.equals(ConfigProvider.getConfig().getValue("service.authMode", AuthMode.class))) {
			return null;
		}
		if (refreshCode == null || refreshCode.isBlank()) {
			return null;
		}
		
		JsonNode response;
		
		try {
			response = ksc.refreshToken(
				ConfigProvider.getConfig().getValue("service.externalAuth.clientId", String.class),
				ConfigProvider.getConfig().getValue("service.externalAuth.clientSecret", String.class),
				"refresh_token",
				refreshCode
			);
		} catch(Throwable e) {
			log.warn("Failed to refresh token from keycloak (exception)- ", e);
			//TODO:: deal with properly
			e.printStackTrace();
			throw e;
		}
		
		log.info("Got response from keycloak on token refresh request: {}", response);
		
		return response;
	}
	
	protected TemplateInstance setupPageTemplate(Template template) {
		return template
			.data("generateDatetime", ZonedDateTime.now())
			.data("dateTimeFormatter", UiUtils.DATE_TIME_FORMATTER);
	}
	
	protected TemplateInstance setupPageTemplate(Template template, UserGetResponse userInfo) {
		return this.setupPageTemplate(template).data(USER_INFO_DATA_KEY, userInfo);
	}
	
	protected TemplateInstance setupPageTemplate(
		Template template,
		UserGetResponse userInfo,
		SearchResult<?> searchResults,
		PagingOptions pageOptions
	) {
		return this.setupPageTemplate(template, userInfo)
				   .data("showSearch", searchResults.isHadSearchQuery())
				   .data("searchResult", searchResults)
				   .data("pagingCalculations", new PagingCalculations(pageOptions, searchResults));
	}
	
}
