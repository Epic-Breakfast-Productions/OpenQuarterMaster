package tech.ebp.oqm.core.api.testResources.data;

import lombok.Data;
import tech.ebp.oqm.core.api.model.object.Versionable;

@Data
public class TestVersionableObject implements Versionable {
	
	@Override
	public int getSchemaVersion() {
		return 4;
	}
	
	private String name;
	private int foo;
	private int bar;
	private int baz;
}
