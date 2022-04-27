package com.ebp.openQuarterMaster.lib.driver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.print.attribute.standard.DateTimeAtCreation;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class ModuleInfo {
	
	@NotNull
	private String serialNo;
	@Past
	private LocalDate manufactureDate;
	private String commSpecVersion;
	@Min(0)
	private int numBlocks;
}
