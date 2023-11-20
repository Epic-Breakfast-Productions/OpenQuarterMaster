package tech.ebp.oqm.baseStation.testResources.data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tech.ebp.oqm.baseStation.interfaces.endpoints.media.FileGet;
import tech.ebp.oqm.baseStation.model.object.FileMainObject;
import tech.ebp.oqm.baseStation.model.object.media.FileMetadata;
import tech.ebp.oqm.baseStation.model.object.media.file.FileAttachment;
import tech.ebp.oqm.baseStation.model.rest.media.file.FileAttachmentGet;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class TestMainFileObjectGet extends TestMainFileObject implements FileGet {
	
	public static TestMainFileObjectGet fromTestFileObject(
		TestMainFileObject a,
		List<FileMetadata> revisions
	) {
		return (TestMainFileObjectGet) new TestMainFileObjectGet()
									   .setRevisions(revisions)
									   .setFileName(a.getFileName())
									   .setAttributes(a.getAttributes())
									   .setKeywords(a.getKeywords())
									   .setId(a.getId());
	}
	
	private List<FileMetadata> revisions;
}
