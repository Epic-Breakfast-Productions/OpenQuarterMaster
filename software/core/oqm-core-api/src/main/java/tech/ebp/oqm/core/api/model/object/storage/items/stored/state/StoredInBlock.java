package tech.ebp.oqm.core.api.model.object.storage.items.stored.state;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoredInBlock extends StoredState {
	
	@Override
	public StoredStateType getType() {
		return StoredStateType.STORED;
	}
	
	/**
	 * The {@link StorageBlock} this stored is stored in.
	 */
	@NonNull
	@NotNull
	@Schema(required = true, description = "The storage block where this item is stored.")
	private ObjectId storageBlock;
}
