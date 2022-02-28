package com.ebp.openQuarterMaster.lib.core.test;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TestOne extends TestSuper<String> {
	
	public TestOne() {
		super(ClassType.ONE, null);
	}
	
	public TestOne(String value) {
		this();
		this.setValue(value);
	}
}
