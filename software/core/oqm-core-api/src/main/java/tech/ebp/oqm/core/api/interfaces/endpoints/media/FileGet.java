package tech.ebp.oqm.core.api.interfaces.endpoints.media;

import tech.ebp.oqm.core.api.model.object.media.FileMetadata;

import java.util.List;

public interface FileGet {
	
	List<FileMetadata> getRevisions();
	
	default int getNumRevisions(){
		return this.getRevisions().size();
	}
	
	default int getLatestRevision(){
		return this.getNumRevisions();
	}
	
}
