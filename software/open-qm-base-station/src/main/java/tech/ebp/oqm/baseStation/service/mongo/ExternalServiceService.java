package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Filters;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.security.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import tech.ebp.oqm.baseStation.config.ExtServicesConfig;
import tech.ebp.oqm.baseStation.rest.search.ExternalServiceSearch;
import tech.ebp.oqm.baseStation.service.JwtService;
import tech.ebp.oqm.baseStation.service.PasswordService;
import tech.ebp.oqm.baseStation.service.mongo.exception.DbNotFoundException;
import tech.ebp.oqm.baseStation.utils.AuthMode;
import tech.ebp.oqm.lib.core.object.history.events.UpdateEvent;
import tech.ebp.oqm.lib.core.object.interactingEntity.externalService.ExternalService;
import tech.ebp.oqm.lib.core.object.interactingEntity.externalService.plugin.Plugin;
import tech.ebp.oqm.lib.core.object.interactingEntity.externalService.plugin.PluginService;
import tech.ebp.oqm.lib.core.rest.externalService.ExternalServiceSetupRequest;
import tech.ebp.oqm.lib.core.rest.externalService.PluginServiceSetupRequest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

@Slf4j
@ApplicationScoped
public class ExternalServiceService extends MongoHistoriedObjectService<ExternalService, ExternalServiceSearch> {
	
	//    private Validator validator;
	private AuthMode authMode;
	private PasswordService passwordService;
	ExtServicesConfig extServicesConfig;
	
	ExternalServiceService() {//required for DI
		super(null, null, null, null, null, null, false, null);
	}
	
	@Inject
	ExternalServiceService(
		//            Validator validator,
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		@ConfigProperty(name = "quarkus.mongodb.database")
		String database,
		@ConfigProperty(name = "service.authMode")
		AuthMode authMode,
		PasswordService passwordService,
		ExtServicesConfig extServicesConfig
	) {
		super(
			objectMapper,
			mongoClient,
			database,
			ExternalService.class,
			true
		);
		this.authMode = authMode;
		this.passwordService = passwordService;
		this.extServicesConfig = extServicesConfig;
		//        this.validator = validator;
	}
	
	@WithSpan
	@Override
	public void ensureObjectValid(boolean newObject, ExternalService newOrChangedObject, ClientSession clientSession) {
		super.ensureObjectValid(newObject, newOrChangedObject, clientSession);
		//TODO:: name not existant
	}
	
	@WithSpan
	private ExternalService getExternalService(String externalSource, String externalId) {
		if (externalId == null) {
			return null;
		}
		return this.getCollection().find(eq("externIds." + externalSource, externalId)).limit(1).first();
	}
	
	@WithSpan
	private ExternalService getExternalService(JsonWebToken jwt) {
		String externalSource = jwt.getIssuer();
		String externalId = jwt.getClaim(Claims.sub);
		log.debug("User id from external jwt: {}", externalId);
		ExternalService externalService = this.getExternalService(externalSource, externalId);
		
		if (externalService != null) {
			//TODO:: update from given jwt, if needed?
			return externalService;
		}
		throw new DbNotFoundException("Make sure the calling service hit the setup endpoint first.", ExternalService.class, null);
	}
	
	@WithSpan
	public ExternalService getFromJwt(JsonWebToken jwt) {
		//TODO:: check is user?
		switch (this.authMode) {
			case SELF:
				log.debug("Getting service data from self.");
				String extServiceId = jwt.getClaim(JwtService.JWT_SERVICE_ID_CLAIM);
				if (extServiceId == null) {
					return null;
				}
				try {
					return this.get(extServiceId);
				} catch(DbNotFoundException e) {
					throw new UnauthorizedException("Service in JWT not found.");
				}
			case EXTERNAL:
				log.debug("Getting external service data ");
				return this.getExternalService(jwt);
		}
		return null;
	}
	
	@WithSpan
	public ExternalService getFromServiceName(String name) {
		ExternalService service = this.getCollection().find(Filters.eq("name", name)).limit(1).first();
		
		if (service == null) {
			throw new DbNotFoundException("No service found with name \"" + name + "\"", this.getClazz(), null);
		}
		
		return service;
	}
	
	@WithSpan
	private ExternalService updateExtServiceFromSetupRequest(ExternalService existentExtService, ExternalServiceSetupRequest setupRequest) {
		if (!existentExtService.getServiceType().equals(setupRequest.getServiceType())) {
			log.debug("Updated external service was a different type than previously.");
			ExternalService newExtService = setupRequest.toExtService();
			
			newExtService.setId(existentExtService.getId());
			newExtService.setAttributes(existentExtService.getAttributes());
			newExtService.setKeywords(existentExtService.getKeywords());
			newExtService.setDisabled(existentExtService.isDisabled());
			
			existentExtService = newExtService;
		} else {
			existentExtService.setDescription(setupRequest.getDescription());
			existentExtService.setDeveloperName(setupRequest.getDeveloperName());
			existentExtService.setDeveloperEmail(setupRequest.getDeveloperEmail());
			
			existentExtService.setRequestedRoles(setupRequest.getRequestedRoles());
			existentExtService.getRoles().retainAll(existentExtService.getRequestedRoles());
			
			switch (existentExtService.getServiceType()) {
				case GENERAL:
					//Nothing extra for general
					break;
				case PLUGIN: //ensure previously enabled plugins still exist, everything else is disabled.
					List<Plugin> origEnabledPlugins = ((PluginService)existentExtService).getEnabledPageComponents();
					LinkedList<Plugin> newPlugins = new LinkedList<>(((PluginServiceSetupRequest)setupRequest).getPageComponents());
					
					((PluginService)existentExtService).setEnabledPageComponents(new ArrayList<>());
					((PluginService)existentExtService).setDisabledPageComponents(new ArrayList<>());
					
					for(Plugin curPlugin : newPlugins){
						if(origEnabledPlugins.contains(curPlugin)){
							((PluginService)existentExtService).getEnabledPageComponents().add(curPlugin);
						} else {
							((PluginService)existentExtService).getDisabledPageComponents().add(curPlugin);
						}
					}
					
					break;
			}
		}
		
		this.update(
			existentExtService,
			existentExtService,
			new UpdateEvent(existentExtService, existentExtService)
				.setDescription("Update from Setup Request.")
		);
		return existentExtService;
	}
	
	@WithSpan
	public ExternalService getFromSetupRequest(ExternalServiceSetupRequest setupRequest) {
		ExternalService existentExtService;
		try {
			existentExtService = this.getFromServiceName(setupRequest.getName());
			
			if (existentExtService.changedGiven(setupRequest)) {
				log.info("Previously seen external service's setup request changed what was stored. Updating.");
				existentExtService = this.updateExtServiceFromSetupRequest(
					existentExtService,
					setupRequest
				);
			}
			
		} catch(DbNotFoundException e) {
			log.info("New external service. Adding to database.");
			existentExtService = setupRequest.toExtService();
			
			this.add(existentExtService);
		}
		
		return existentExtService;
	}
	
}
