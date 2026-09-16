package tech.ebp.oqm.core.api.model.rest.media.file;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import tech.ebp.oqm.core.api.interfaces.endpoints.media.FileGet;
import tech.ebp.oqm.core.api.model.object.media.FileMetadata;
import tech.ebp.oqm.core.api.model.object.media.file.FileAttachment;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "Describes a file attachment's metadata.")
public class FileAttachmentGet extends FileAttachment implements FileGet {
	
	public static FileAttachmentGet fromFileAttachment(
		FileAttachment a,
		List<FileMetadata> revisions
	) {
		return (FileAttachmentGet) (
			(FileAttachmentGet) new FileAttachmentGet()
									.setFileName(a.getFileName())
									.setDescription(a.getDescription())
		)
									   .setRevisions(revisions)
									   .setAttributes(a.getAttributes())
									   .setKeywords(a.getKeywords())
									   .setId(a.getId());
	}
	
	private List<FileMetadata> revisions;
	
}
