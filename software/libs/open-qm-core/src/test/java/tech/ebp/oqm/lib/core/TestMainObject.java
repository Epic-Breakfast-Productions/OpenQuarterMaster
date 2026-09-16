package tech.ebp.oqm.lib.core;

import tech.ebp.oqm.lib.core.object.AttKeywordMainObject;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
public class TestMainObject extends AttKeywordMainObject {
	
	public TestMainObject(ObjectId objectId, Map<String, String> atts, List<String> keywords) {
		super(objectId, atts, keywords);
	}
}
