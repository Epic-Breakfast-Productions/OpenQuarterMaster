package tech.ebp.oqm.baseStation.model;

import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.object.AttKeywordMainObject;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
public class TestMainObject extends AttKeywordMainObject {
	
	public TestMainObject(ObjectId objectId, Map<String, String> atts, List<String> keywords) {
		super(objectId, atts, keywords);
	}
}
