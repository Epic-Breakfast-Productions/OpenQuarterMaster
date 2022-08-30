package com.ebp.openQuarterMaster.baseStation.testResources.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import tech.ebp.oqm.lib.core.object.AttKeywordMainObject;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestMainObject extends AttKeywordMainObject {
	
	@NotNull
	@NotEmpty
	private String testField;
}
