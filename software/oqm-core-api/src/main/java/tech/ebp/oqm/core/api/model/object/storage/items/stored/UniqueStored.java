package tech.ebp.oqm.core.api.model.object.storage.items.stored;

import jakarta.validation.constraints.NotNull;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.FileAttachmentContaining;

import java.util.Set;

public class UniqueStored extends Stored {
	@Override
	public StoredType getStoredType() {
		return StoredType.UNIQUE;
	}

	@Override
	public String getLabelText() {
		//TODO:: something better
		return this.getId().toHexString();
	}

	@Override
	public int getSchemaVersion() {
		return 1;
	}
}
