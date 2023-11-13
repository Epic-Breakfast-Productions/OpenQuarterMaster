package tech.ebp.oqm.baseStation.model.rest.media.file;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tech.ebp.oqm.baseStation.model.object.media.FileMetadata;
import tech.ebp.oqm.baseStation.model.object.media.file.FileAttachment;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FileAttachmentGet extends FileAttachment {
	
	public static FileAttachmentGet fromFileAttachment(
		FileAttachment a,
		List<FileMetadata> revisions
	) {
		return (FileAttachmentGet) new FileAttachmentGet()
									   .setRevisions(revisions)
									   .setFileName(a.getFileName())
									   .setAttributes(a.getAttributes())
									   .setKeywords(a.getKeywords())
									   .setId(a.getId());
	}
	
	private List<FileMetadata> revisions;
	
}
