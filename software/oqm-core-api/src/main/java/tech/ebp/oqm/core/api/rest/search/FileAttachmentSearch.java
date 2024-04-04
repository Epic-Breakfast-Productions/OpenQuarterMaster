package tech.ebp.oqm.core.api.rest.search;

import lombok.Getter;
import lombok.ToString;
import tech.ebp.oqm.core.api.model.object.media.file.FileAttachment;

@ToString(callSuper = true)
@Getter
public class FileAttachmentSearch extends FileSearchObject<FileAttachment> {
	//TODO:: add to bson filter list
}
