package tech.ebp.oqm.core.api.service.mongo;

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
import tech.ebp.oqm.core.api.config.CoreApiInteractingEntity;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.rest.search.InteractingEntitySearch;

import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

/**
 * Service to keep track of interacting entities that have interacted with the service.
 */
@Slf4j
@Named("InteractingEntityService")
@ApplicationScoped
public class InteractingEntityService extends TopLevelMongoService<InteractingEntity, InteractingEntitySearch, CollectionStats> {
	
	@ConfigProperty(name = "quarkus.http.auth.basic", defaultValue = "false")
	boolean basicAuthEnabled;
	
	/**
	 * Lock to ensure concurrency when ensuring a user exists.
	 */
	private final ReentrantLock ensureUserLock = new ReentrantLock();
	
	public InteractingEntityService() {
		super(InteractingEntity.class);
	}
	
	@PostConstruct
	public void setup() {
		//TODO:: ponder if CDI really necessary?
		CoreApiInteractingEntity coreApiInteractingEntityArc;
		try (InstanceHandle<CoreApiInteractingEntity> container = Arc.container().instance(CoreApiInteractingEntity.class)) {
			coreApiInteractingEntityArc = container.get();
		}
		if (coreApiInteractingEntityArc == null) {
			return;
		}
		//force getting around Arc subclassing out the injected class
		CoreApiInteractingEntity coreApiInteractingEntity = new CoreApiInteractingEntity();
		//ensure we have the base station in the db
		CoreApiInteractingEntity gotten = (CoreApiInteractingEntity) this.get(coreApiInteractingEntity.getId());
		if (gotten == null) {
			this.add(coreApiInteractingEntity);
			log.info("Added core api interacting entity entry.");
		} else {
			this.update(coreApiInteractingEntity);
			log.info("Updated core api interacting entity entry.");
		}
	}
	
	
	private Optional<InteractingEntity> get(SecurityContext securityContext, JsonWebToken jwt) {
		Bson query;
		if (this.basicAuthEnabled) {
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
	
	public InteractingEntity get(ObjectId id) {
		return this.getTypedCollection().find(eq("_id", id)).limit(1).first();
	}
	
	public InteractingEntity get(String id) {
		return this.get(new ObjectId(id));
	}
	
	public ObjectId add(@Valid InteractingEntity entity) {
		return this.getTypedCollection().insertOne(entity).getInsertedId().asObjectId().getValue();
	}
	
	protected void update(InteractingEntity entity) {
		this.getTypedCollection().findOneAndReplace(eq("_id", entity.getId()), entity);
	}
	
	/**
	 * Ensures the entity exists in the system. Retrieves the entity from the request data given.
	 * <p>
	 * TODO:: use {@link InstanceMutexService} to ensure cross-service concurrency
	 *
	 * @param context
	 * @param jwt
	 *
	 * @return The entity that is interacting with the system, guaranteed to be in the database and updated based on request data.
	 */
	public InteractingEntity ensureEntity(SecurityContext context, JsonWebToken jwt) {
		InteractingEntity entity = null;
		try { //TODO:: test this for performance. Any way around making the whole thing a critical section?
			this.ensureUserLock.lock();
			
			Optional<InteractingEntity> returningEntityOp = this.get(context, jwt);
			if (returningEntityOp.isEmpty()) {
				log.info("New entity interacting with system.");
				if (this.basicAuthEnabled) {
					entity = InteractingEntity.createEntity(context);
				} else {
					entity = InteractingEntity.createEntity(jwt);
				}
			} else {
				log.debug("Existing entity interacting with system.");
				entity = returningEntityOp.get();
			}
			
			if (entity.getId() == null) {
				this.add(entity);
			} else if (!this.basicAuthEnabled && entity.updateFrom(jwt)) {
				this.update(entity);
				log.info("Entity has been updated.");
			}
		} finally {
			this.ensureUserLock.unlock();
		}
		
		return entity;
	}
	
	/**
	 * Gets the entity behind a particular event.
	 *
	 * @param e The event in question
	 *
	 * @return The entity that performed the event.
	 */
	public InteractingEntity get(ObjectHistoryEvent e) {
		return this.get(e.getEntity());
	}
	
	@Override
	public int getCurrentSchemaVersion() {
		return InteractingEntity.CUR_SCHEMA_VERSION;
	}
}
