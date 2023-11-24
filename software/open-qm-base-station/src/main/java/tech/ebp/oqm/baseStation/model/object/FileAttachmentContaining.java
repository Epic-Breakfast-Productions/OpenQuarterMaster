package tech.ebp.oqm.baseStation.model.object;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FileAttachmentContaining {
	
	public Set<@NotNull ObjectId> getAttachedFiles();
	
	public FileAttachmentContaining setAttachedFiles(Set<@NotNull ObjectId> attachedFiles);

}
