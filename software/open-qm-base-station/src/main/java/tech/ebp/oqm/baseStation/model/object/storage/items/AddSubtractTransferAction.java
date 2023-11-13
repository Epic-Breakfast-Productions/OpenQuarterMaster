package tech.ebp.oqm.baseStation.model.object.storage.items;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.object.storage.items.stored.Stored;

import java.util.UUID;

/**
 * TODO:: validate state based on action type
 */
@Data
@NoArgsConstructor
public class AddSubtractTransferAction {
	
	@NonNull
	@NotNull
	private AddSubtractTransferActionType actionType;

	private ObjectId storageBlockFrom;
	private ObjectId storageBlockTo;
	
	private UUID storedIdFrom;
	private UUID storedIdTo;
	
	private Stored toMove;
	
	public boolean toStored(){
		return this.storedIdTo != null;
	}
	public boolean fromStored(){
		return this.storedIdTo != null;
	}
	public boolean fromToStored(){
		return this.fromStored() || this.toStored();
	}
	
}
