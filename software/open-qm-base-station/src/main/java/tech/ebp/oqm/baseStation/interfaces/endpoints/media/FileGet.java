package tech.ebp.oqm.baseStation.interfaces.endpoints.media;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.ebp.oqm.baseStation.model.object.media.FileMetadata;

import java.util.List;

public interface FileGet {
	
	public List<FileMetadata> getRevisions();
	
	@JsonIgnore
	public default FileMetadata getLatestRevision(){
		return this.getRevisions().get(0);
	}

}
