package com.ebp.openQuarterMaster.lib.driver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ModuleInfo {
	
	@NotNull
	private String serialNo;
	@Min(0)
	private int numBlocks;
	private String storageLayoutId;
	@Past
	private ZonedDateTime firstFound;
	@Past
	private ZonedDateTime lastComm;
}
