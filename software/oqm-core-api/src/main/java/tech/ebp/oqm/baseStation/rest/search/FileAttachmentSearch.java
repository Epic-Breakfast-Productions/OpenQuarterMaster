package tech.ebp.oqm.baseStation.rest.search;

import jakarta.ws.rs.HeaderParam;
import lombok.Getter;
import lombok.ToString;
import tech.ebp.oqm.baseStation.model.object.media.file.FileAttachment;

@ToString(callSuper = true)
@Getter
public class FileAttachmentSearch extends SearchObject<FileAttachment> {
	//TODO:: add to bson filter list
	
	
}