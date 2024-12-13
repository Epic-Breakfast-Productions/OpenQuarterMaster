package tech.ebp.oqm.core.api.model.object.history;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.AttKeywordMainObject;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.object.history.details.HistoryDetail;
import tech.ebp.oqm.core.api.model.object.history.details.HistoryDetailType;
import tech.ebp.oqm.core.api.model.object.history.events.CreateEvent;
import tech.ebp.oqm.core.api.model.object.history.events.DeleteEvent;
import tech.ebp.oqm.core.api.model.object.history.events.ReCreateEvent;
import tech.ebp.oqm.core.api.model.object.history.events.UpdateEvent;
import tech.ebp.oqm.core.api.model.object.history.events.file.NewFileVersionEvent;
import tech.ebp.oqm.core.api.model.object.history.events.item.ItemLowStockEvent;
import tech.ebp.oqm.core.api.model.object.history.events.item.expiry.ItemExpiredEvent;
import tech.ebp.oqm.core.api.model.object.history.events.item.expiry.ItemExpiryWarningEvent;
import tech.ebp.oqm.core.api.model.object.history.events.itemList.ItemListActionAddEvent;
import tech.ebp.oqm.core.api.model.object.history.events.itemList.ItemListActionDeleteEvent;
import tech.ebp.oqm.core.api.model.object.history.events.itemList.ItemListActionUpdateEvent;
import tech.ebp.oqm.core.api.model.object.history.events.itemList.ItemListApplyEvent;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Describes an object's history.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = CreateEvent.class, name = "CREATE"),
	@JsonSubTypes.Type(value = ReCreateEvent.class, name = "RECREATE"),
	@JsonSubTypes.Type(value = UpdateEvent.class, name = "UPDATE"),
	@JsonSubTypes.Type(value = DeleteEvent.class, name = "DELETE"),
	
	@JsonSubTypes.Type(value = ItemExpiryWarningEvent.class, name = "ITEM_EXPIRY_WARNING"),
	@JsonSubTypes.Type(value = ItemExpiredEvent.class, name = "ITEM_EXPIRED"),
	@JsonSubTypes.Type(value = ItemLowStockEvent.class, name = "ITEM_LOW_STOCK"),
	
	@JsonSubTypes.Type(value = ItemListActionAddEvent.class, name = "ITEM_LIST_ACTION_ADD"),
	@JsonSubTypes.Type(value = ItemListActionUpdateEvent.class, name = "ITEM_LIST_ACTION_UPDATE"),
	@JsonSubTypes.Type(value = ItemListActionDeleteEvent.class, name = "ITEM_LIST_ACTION_REMOVE"),
	@JsonSubTypes.Type(value = ItemListApplyEvent.class, name = "ITEM_LIST_APPLY"),
	
	@JsonSubTypes.Type(value = NewFileVersionEvent.class, name = "FILE_NEW_VERSION"),
})
@BsonDiscriminator
@SuperBuilder(toBuilder = true)
public abstract class ObjectHistoryEvent extends AttKeywordMainObject {
	
	/**
	 * The id of the object this history is for
	 */
	@NotNull
	private ObjectId objectId;
	
	/**
	 * The interacting entity that performed the event. Not required to be anything, as in some niche cases there wouldn't be one (adding user)
	 */
	@lombok.Builder.Default
	private ObjectId entity = null;
	
	/**
	 * When the event occurred
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private ZonedDateTime timestamp = ZonedDateTime.now();

	@NonNull
	@NotNull
	@lombok.Builder.Default
	private Map<String, HistoryDetail> details = new HashMap<>();
	
	public abstract EventType getType();
	
	public ObjectHistoryEvent(ObjectId objectId, InteractingEntity entity) {
		this.objectId = objectId;
		if(entity != null) {
			this.entity = entity.getId();
		}
	}
	
	public ObjectHistoryEvent(MainObject object, InteractingEntity entity) {
		this(object.getId(), entity);
	}
	
	public ObjectHistoryEvent setEntity(ObjectId interactingEntityId){
		this.entity = interactingEntityId;
		return this;
	}
}
