package tech.ebp.oqm.core.api.model.object;

import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Set;

public interface ImageAttachmentContaining {

	public List<@NonNull ObjectId> getImageIds();

	public ImageAttachmentContaining setImageIds(List<@NonNull ObjectId> attachedImages);

}
