package tech.ebp.oqm.baseStation.model.objectUpgrade.testUtil;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.ebp.oqm.baseStation.model.object.Versionable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestVersionable implements Versionable {
	
	
	public static final String VERSION_ONE_JSON = """
		{
			"testStringOld": "hello world"
			"schemaVersion": 1
		}
		""";
	
	@Override
	public int getSchemaVersion() {
		return 4;
	}
	
	private int testInt;
	private String testString;
	private int testDouble;
}
