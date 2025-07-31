package tech.ebp.oqm.core.api.model;

import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.AttKeywordMainObject;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
public class TestMainObject extends AttKeywordMainObject {
	
	public TestMainObject(ObjectId objectId, Map<String, String> atts, List<String> keywords) {
		super(objectId, atts, keywords);
	}

	@Override
	public int getSchemaVersion() {
		return 1;
	}
}
