package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import tech.ebp.oqm.baseStation.config.BaseStationInteractingEntity;
import tech.ebp.oqm.baseStation.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntityReference;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntityType;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.user.NotificationSettings;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.user.User;
import tech.ebp.oqm.baseStation.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.baseStation.model.rest.auth.roles.Roles;
import tech.ebp.oqm.baseStation.rest.search.InteractingEntitySearch;
import tech.ebp.oqm.baseStation.service.notification.item.ItemLowStockEventNotificationService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Optional;
import java.util.Set;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

@Slf4j
@Named("InteractingEntityService")
@ApplicationScoped
public class InteractingEntityService extends MongoObjectService<InteractingEntity, InteractingEntitySearch> {
	
	InteractingEntityService() {//required for DI
		super(null, null, null, null, null, null);
	}
	
	@Inject
	InteractingEntityService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		@ConfigProperty(name = "quarkus.mongodb.database")
		String database
	) {
		super(
			objectMapper,
			mongoClient,
			database,
			InteractingEntity.class
		);
	}
	
	private Optional<InteractingEntity> getEntity(String authProvider, String idFromAuthProvider) {
		return Optional.ofNullable(
			this.listIterator(
					and(
						eq("authProvider", authProvider),
						eq("idFromAuthProvider", idFromAuthProvider)
					),
					null,
					null
				)
				.limit(1)
				.first()
		);
	}
	
	@WithSpan
	public InteractingEntity getEntity(JsonWebToken jwt) {
		String authProvider = jwt.getIssuer();
		String idFromAuthProvider = jwt.getSubject();
		
		InteractingEntity entity = null;
		Optional<InteractingEntity> returningEntityOp = this.getEntity(authProvider, idFromAuthProvider);
		if(returningEntityOp.isEmpty()){
			log.info("New entity interacting with system.");
			entity = InteractingEntity.createEntity(jwt);
		} else {
			log.info("Returning entity interacting with system.");
			entity = returningEntityOp.get();
		}
		
		if(entity.getId() == null){
			this.add(entity);
		} else if (entity.updateFrom(jwt)) {
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
