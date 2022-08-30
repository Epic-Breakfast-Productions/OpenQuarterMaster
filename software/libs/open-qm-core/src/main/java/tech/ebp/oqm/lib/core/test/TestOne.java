package tech.ebp.oqm.lib.core.test;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Deprecated
public class TestOne extends TestSuper<String> {
	
	public TestOne() {
		super(ClassType.ONE, null);
	}
	
	public TestOne(String value) {
		this();
		this.setValue(value);
	}
}
