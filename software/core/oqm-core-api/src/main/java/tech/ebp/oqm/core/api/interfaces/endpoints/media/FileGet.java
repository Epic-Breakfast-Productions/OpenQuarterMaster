package tech.ebp.oqm.core.api.interfaces.endpoints.media;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import tech.ebp.oqm.core.api.model.object.media.FileMetadata;

import java.util.List;

public interface FileGet {

	@Schema(description = "The file's revisions. Higher index is the more recent revision.")
	List<FileMetadata> getRevisions();

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Schema(description = "The number of revisions.")
	default int getNumRevisions(){
		return this.getRevisions().size();
	}

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Schema(description = "The latest revision index, usable in API calls where revisions are specified.")
	default int getLatestRevision(){
		return this.getNumRevisions();
	}

}
