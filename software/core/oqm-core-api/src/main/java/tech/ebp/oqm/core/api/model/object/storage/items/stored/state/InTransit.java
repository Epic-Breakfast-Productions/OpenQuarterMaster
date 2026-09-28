package tech.ebp.oqm.core.api.model.object.storage.items.stored.state;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import tech.ebp.oqm.core.api.model.object.AttKeywordContaining;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "InTransit", description = "The state to specify this stored is not yet stored, but in transit to another location.")
public class InTransit extends StoredState {

	@Override
	public StoredStateType getType() {
		return StoredStateType.IN_TRANSIT;
	}

}
