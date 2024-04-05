package tech.ebp.oqm.core.api.interfaces.endpoints.media;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.ebp.oqm.core.api.model.object.media.FileMetadata;

import java.util.List;

public interface FileGet {
	
	public List<FileMetadata> getRevisions();
	
	@JsonIgnore
	public default FileMetadata getLatestRevision(){
		return this.getRevisions().get(0);
	}

}
