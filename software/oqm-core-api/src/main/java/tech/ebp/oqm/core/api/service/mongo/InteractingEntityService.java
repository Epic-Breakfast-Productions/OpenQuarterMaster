package tech.ebp.oqm.core.api.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import tech.ebp.oqm.core.api.config.BaseStationInteractingEntity;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.rest.search.InteractingEntitySearch;
import tech.ebp.oqm.core.api.service.mongo.exception.DbNotFoundException;

import java.util.Optional;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

@Slf4j
@Named("InteractingEntityService")
@ApplicationScoped
public class InteractingEntityService extends MongoObjectService<InteractingEntity, InteractingEntitySearch, CollectionStats> {
	
	private boolean basicAuthEnabled = false;
	
	InteractingEntityService() {//required for DI
		super(null, null, null, null, null, null);
	}
	
	@Inject
	InteractingEntityService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		@ConfigProperty(name = "quarkus.mongodb.database")
		String database,
		BaseStationInteractingEntity baseStationInteractingEntityArc,
		@ConfigProperty(name = "quarkus.http.auth.basic", defaultValue = "false")
		boolean basicAuthEnabled
	) {
		super(
			objectMapper,
			mongoClient,
			database,
			InteractingEntity.class
		);
		this.basicAuthEnabled = basicAuthEnabled;
		//force getting around Arc subclassing out the injected class
		BaseStationInteractingEntity baseStationInteractingEntity = new BaseStationInteractingEntity(
			baseStationInteractingEntityArc.getEmail()
		);
		//ensure we have the base station in the db
		try{
			this.get(baseStationInteractingEntity.getId());
			this.update(baseStationInteractingEntity);
			log.info("Updated base station interacting entity entry.");
		} catch(DbNotFoundException e){
			this.add(baseStationInteractingEntity);
			log.info("Added base station interacting entity entry.");
		}
	}
	
	@Override
	public CollectionStats getStats() {
		return super.addBaseStats(CollectionStats.builder())
				   .build();
	}
	
	private Optional<InteractingEntity> getEntityFromDb(SecurityContext securityContext, JsonWebToken jwt) {
		Bson query;
		if(this.basicAuthEnabled){
			query = eq("name", securityContext.getUserPrincipal().getName());
		} else {
			query = and(
				eq("authProvider", jwt.getIssuer()),
				eq("idFromAuthProvider", jwt.getSubject())
			);
		}
		
		return Optional.ofNullable(
			this.listIterator(
					query,
					null,
					null
				)
				.limit(1)
				.first()
		);
	}
	
	@WithSpan
	public InteractingEntity getEntity(SecurityContext context, JsonWebToken jwt) {
		InteractingEntity entity = null;
		Optional<InteractingEntity> returningEntityOp = this.getEntityFromDb(context, jwt);
		if(returningEntityOp.isEmpty()){
			log.info("New entity interacting with system.");
			if(this.basicAuthEnabled){
				entity = InteractingEntity.createEntity(context);
			} else {
				entity = InteractingEntity.createEntity(jwt);
				
			}
			
		} else {
			log.info("Returning entity interacting with system.");
			entity = returningEntityOp.get();
		}
		
		if(entity.getId() == null){
			this.add(entity);
		} else if (!this.basicAuthEnabled && entity.updateFrom(jwt)) {
			this.update(entity);
			log.info("Entity has been updated.");
		}
		
		return entity;
	}
	
	
	@WithSpan
	public InteractingEntity getEntity(ObjectHistoryEvent e) {
		return this.get(e.getEntity());
	}
}
