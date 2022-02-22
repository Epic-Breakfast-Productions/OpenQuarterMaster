package com.ebp.openQuarterMaster.lib.core.test;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TestTwo extends TestSuper<Integer> {
	
	public TestTwo() {
		super(ClassType.TWO, null);
	}
	
	public TestTwo(Integer value) {
		this();
		this.setValue(value);
	}
}