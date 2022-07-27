package com.ebp.openQuarterMaster.baseStation.testResources.data;

import com.ebp.openQuarterMaster.lib.core.AttKeywordMainObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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
