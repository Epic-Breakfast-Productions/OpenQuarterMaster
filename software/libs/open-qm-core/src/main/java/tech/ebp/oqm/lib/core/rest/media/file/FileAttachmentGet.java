package tech.ebp.oqm.lib.core.rest.media.file;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tech.ebp.oqm.lib.core.object.media.FileMetadata;
import tech.ebp.oqm.lib.core.object.media.file.FileAttachment;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FileAttachmentGet extends FileAttachment {
	
	private List<FileMetadata> revisions;
}
