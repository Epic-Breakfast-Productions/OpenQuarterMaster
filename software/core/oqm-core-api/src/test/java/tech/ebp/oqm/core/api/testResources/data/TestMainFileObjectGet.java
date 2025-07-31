package tech.ebp.oqm.core.api.testResources.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tech.ebp.oqm.core.api.interfaces.endpoints.media.FileGet;
import tech.ebp.oqm.core.api.model.object.media.FileMetadata;

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
									   .setAttributes(a.getAttributes())
									   .setKeywords(a.getKeywords())
									   .setId(a.getId());
	}
	
	private List<FileMetadata> revisions;
}
