package tech.ebp.oqm.lib.core.object.history;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonRepresentation;
import tech.ebp.oqm.lib.core.object.AttKeywordMainObject;
import tech.ebp.oqm.lib.core.object.history.events.DeleteEvent;
import tech.ebp.oqm.lib.core.object.history.events.UpdateEvent;
import tech.ebp.oqm.lib.core.object.history.events.externalService.ExtServiceAuthEvent;
import tech.ebp.oqm.lib.core.object.history.events.externalService.ExtServiceSetupEvent;
import tech.ebp.oqm.lib.core.object.history.events.item.ItemAddEvent;
import tech.ebp.oqm.lib.core.object.history.events.item.ItemLowStockEvent;
import tech.ebp.oqm.lib.core.object.history.events.item.ItemSubEvent;
import tech.ebp.oqm.lib.core.object.history.events.item.ItemTransferEvent;
import tech.ebp.oqm.lib.core.object.history.events.item.expiry.ItemExpiredEvent;
import tech.ebp.oqm.lib.core.object.history.events.item.expiry.ItemExpiryWarningEvent;
import tech.ebp.oqm.lib.core.object.history.events.user.UserDisabledEvent;
import tech.ebp.oqm.lib.core.object.history.events.user.UserEnabledEvent;
import tech.ebp.oqm.lib.core.object.history.events.user.UserLoginEvent;
import tech.ebp.oqm.lib.core.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.lib.core.object.MainObject;
import tech.ebp.oqm.lib.core.object.history.events.CreateEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.object.interactingEntity.InteractingEntityReference;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;

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
	@JsonSubTypes.Type(value = UpdateEvent.class, name = "UPDATE"),
	@JsonSubTypes.Type(value = DeleteEvent.class, name = "DELETE"),
	
	@JsonSubTypes.Type(value = UserLoginEvent.class, name = "USER_LOGIN"),
	@JsonSubTypes.Type(value = UserEnabledEvent.class, name = "USER_ENABLED"),
	@JsonSubTypes.Type(value = UserDisabledEvent.class, name = "USER_DISABLED"),
	
	@JsonSubTypes.Type(value = ItemExpiryWarningEvent.class, name = "ITEM_EXPIRY_WARNING"),
	@JsonSubTypes.Type(value = ItemExpiredEvent.class, name = "ITEM_EXPIRED"),
	@JsonSubTypes.Type(value = ItemLowStockEvent.class, name = "ITEM_LOW_STOCK"),
	@JsonSubTypes.Type(value = ItemAddEvent.class, name = "ITEM_ADD"),
	@JsonSubTypes.Type(value = ItemSubEvent.class, name = "ITEM_SUBTRACT"),
	@JsonSubTypes.Type(value = ItemTransferEvent.class, name = "ITEM_TRANSFER"),
	
	@JsonSubTypes.Type(value = ExtServiceSetupEvent.class, name = "EXT_SERVICE_SETUP"),
	@JsonSubTypes.Type(value = ExtServiceAuthEvent.class, name = "EXT_SERVICE_AUTH"),
})
@BsonDiscriminator
public abstract class ObjectHistoryEvent extends AttKeywordMainObject {
	
	/**
	 * The id of the object this history is for
	 */
	@NotNull
	private ObjectId objectId;
	
	/**
	 * The interacting entity that performed the event. Not required to be anything, as in some niche cases there wouldn't be one (adding user)
	 */
	private InteractingEntityReference entity = null;
	
	/**
	 * When the event occurred
	 */
	@NonNull
	@NotNull
	private ZonedDateTime timestamp = ZonedDateTime.now();
	
	public abstract EventType getType();
	
	public ObjectHistoryEvent(ObjectId objectId, InteractingEntity entity) {
		this.objectId = objectId;
		if(entity != null) {
			this.entity = entity.getReference();
		}
	}
	
	public ObjectHistoryEvent(MainObject object, InteractingEntity entity) {
		this(object.getId(), entity);
	}
}
