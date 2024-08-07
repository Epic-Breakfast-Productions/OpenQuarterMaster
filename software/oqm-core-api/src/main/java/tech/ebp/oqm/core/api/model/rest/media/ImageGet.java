package tech.ebp.oqm.core.api.model.rest.media;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tech.ebp.oqm.core.api.interfaces.endpoints.media.FileGet;
import tech.ebp.oqm.core.api.model.object.media.FileMetadata;
import tech.ebp.oqm.core.api.model.object.media.Image;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ImageGet extends Image implements FileGet {
	
	public static ImageGet fromImage(
		Image a,
		List<FileMetadata> revisions
	) {
		return (ImageGet) (
			(ImageGet) new ImageGet()
									.setDescription(a.getDescription())
						   .setSource(a.getSource())
						   .setFileName(a.getFileName())
		)
							  
									   .setRevisions(revisions)
									   .setAttributes(a.getAttributes())
									   .setKeywords(a.getKeywords())
									   .setId(a.getId());
	}
	
	private List<FileMetadata> revisions;
	
}
