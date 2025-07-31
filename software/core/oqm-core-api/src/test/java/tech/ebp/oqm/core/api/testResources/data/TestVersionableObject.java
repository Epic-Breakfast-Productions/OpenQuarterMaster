package tech.ebp.oqm.core.api.testResources.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.object.Versionable;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class TestVersionableObject extends MainObject implements Versionable {
	
	@Override
	public int getSchemaVersion() {
		return 4;
	}
	
	private String name;
	private int foo;
	private int bar;
	private int baz;
}
