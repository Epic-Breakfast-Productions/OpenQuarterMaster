package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import tech.ebp.oqm.baseStation.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntityReference;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntityType;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.user.NotificationSettings;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.user.User;
import tech.ebp.oqm.baseStation.model.rest.auth.roles.Roles;
import tech.ebp.oqm.baseStation.rest.search.InteractingEntitySearch;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Optional;
import java.util.Set;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

@Named("InteractingEntityService")
@ApplicationScoped
public class InteractingEntityService extends MongoHistoriedObjectService<InteractingEntity, InteractingEntitySearch> {
	
	@Inject
	public InteractingEntityService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		String database,
		String collectionName,
		Class<InteractingEntity> clazz,
		MongoCollection<InteractingEntity> collection,
		boolean allowNullEntityForCreate,
		MongoHistoryService<InteractingEntity> historyService
	) {
		super(objectMapper, mongoClient, database, collectionName, clazz, collection, allowNullEntityForCreate, historyService);
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
		String idFromAuthProvider = jwt.getSubject();// jwt.getClaim(Claims.sub);
		String email = jwt.getClaim(Claims.email);
		String name = jwt.getName();
		String userName = jwt.getClaim(Claims.preferred_username);
		Set<String> groups = jwt.getGroups();
		
		
		InteractingEntity entity = null;
		Optional<InteractingEntity> returningEntityOp = this.getEntity(authProvider, idFromAuthProvider);
		if(returningEntityOp.isEmpty()){
			//TODO:: 361 determine if service account or not
			
			entity = new User(
				name,
				userName,
				email,
				new NotificationSettings(),
				groups
			);
			entity.setAuthProvider(authProvider);
			entity.setIdFromAuthProvider(idFromAuthProvider);
			
			this.add(entity, entity);
		} else {
			entity = returningEntityOp.get();
		}
		
		//TODO:: 361 update entity details
		
		return entity;
	}
	
	
	@WithSpan
	public InteractingEntity getEntity(ObjectHistoryEvent e) {
		return this.get(e.getEntity());
	}
}
