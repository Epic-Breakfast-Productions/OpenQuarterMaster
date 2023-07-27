package tech.ebp.oqm.baseStation.interfaces.ui.pages;

import com.fasterxml.jackson.databind.JsonNode;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.jwt.JsonWebToken;
import tech.ebp.oqm.baseStation.rest.restCalls.KeycloakServiceCaller;
import tech.ebp.oqm.baseStation.service.mongo.search.PagingCalculations;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchResult;
import tech.ebp.oqm.baseStation.utils.AuthMode;
import tech.ebp.oqm.baseStation.model.rest.user.UserGetResponse;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.time.ZonedDateTime;
import java.util.Currency;

import static tech.ebp.oqm.baseStation.utils.AuthMode.EXTERNAL;


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
	
	@Getter
	@Context
	protected UriInfo uri;
	
	protected TemplateInstance setupPageTemplate(Template template, Span span) {
		return template
				   .data("traceId", span.getSpanContext().getTraceId())
				   .data("currency", ConfigProvider.getConfig().getValue("service.ops.currency", Currency.class))
				   .data("generateDatetime", ZonedDateTime.now())
				   .data("dateTimeFormatter", UiUtils.DATE_TIME_FORMATTER);
	}
	
	protected TemplateInstance setupPageTemplate(Template template, Span span, UserGetResponse userInfo) {
		return this.setupPageTemplate(template, span).data(USER_INFO_DATA_KEY, userInfo);
	}
	
	protected TemplateInstance setupPageTemplate(
		Template template,
		Span span,
		UserGetResponse userInfo,
		SearchResult<?> searchResults
	) {
		return this.setupPageTemplate(template, span, userInfo)
				   .data("showSearch", searchResults.isHadSearchQuery())
				   .data("searchResult", searchResults)
				   .data("pagingCalculations", new PagingCalculations(searchResults));
	}
	
}
