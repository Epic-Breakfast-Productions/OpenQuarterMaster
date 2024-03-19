package tech.ebp.oqm.baseStation.rest.search;

import lombok.Getter;
import lombok.ToString;
import tech.ebp.oqm.baseStation.model.object.media.file.FileAttachment;

@ToString(callSuper = true)
@Getter
public class FileAttachmentSearch extends FileSearchObject<FileAttachment> {
	//TODO:: add to bson filter list
}
