package tech.ebp.oqm.core.api.model.object;

import jakarta.validation.constraints.NotNull;
import org.bson.types.ObjectId;

import java.util.Set;

public interface FileAttachmentContaining {
	
	public Set<@NotNull ObjectId> getAttachedFiles();
	
	public FileAttachmentContaining setAttachedFiles(Set<@NotNull ObjectId> attachedFiles);

}
