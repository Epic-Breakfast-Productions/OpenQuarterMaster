package tech.ebp.oqm.core.api.service.mongo;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import tech.ebp.oqm.core.api.config.BaseStationInteractingEntity;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.rest.search.InteractingEntitySearch;
import tech.ebp.oqm.core.api.service.mongo.exception.DbNotFoundException;

import java.util.Optional;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

@Slf4j
@Named("InteractingEntityService")
@ApplicationScoped
public class InteractingEntityService extends TopLevelMongoService<InteractingEntity, InteractingEntitySearch, CollectionStats> {
	
	@ConfigProperty(name = "quarkus.http.auth.basic", defaultValue = "false")
	boolean basicAuthEnabled;
	
	public InteractingEntityService() {
		super(InteractingEntity.class);
		
		
	}
	
	@PostConstruct
	public void setup(){
		BaseStationInteractingEntity baseStationInteractingEntityArc;
		try(InstanceHandle<BaseStationInteractingEntity> container = Arc.container().instance(BaseStationInteractingEntity.class)){
			baseStationInteractingEntityArc = container.get();
		}
		if(baseStationInteractingEntityArc == null){
			return;
		}
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
	
	
	private Optional<InteractingEntity> get(SecurityContext securityContext, JsonWebToken jwt) {
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
	
	public InteractingEntity get(ObjectId id){
		return this.getCollection().find(eq("_id", id)).limit(1).first();
	}

	public InteractingEntity get(String id){
		return this.get(new ObjectId(id));
	}
	
	public ObjectId add(@Valid InteractingEntity entity){
		return this.getCollection().insertOne(entity).getInsertedId().asObjectId().getValue();
	}
	
	protected void update(InteractingEntity entity){
		this.getCollection().findOneAndReplace(eq("_id", entity.getId()), entity);
	}
	
	@WithSpan
	public InteractingEntity ensureEntity(SecurityContext context, JsonWebToken jwt) {
		InteractingEntity entity = null;
		Optional<InteractingEntity> returningEntityOp = this.get(context, jwt);
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
	public InteractingEntity get(ObjectHistoryEvent e) {
		return this.get(e.getEntity());
	}
}
