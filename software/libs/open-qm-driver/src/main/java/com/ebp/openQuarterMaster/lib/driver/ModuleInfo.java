package com.ebp.openQuarterMaster.lib.driver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.print.attribute.standard.DateTimeAtCreation;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Basic information about a storage module.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class ModuleInfo {
	
	/**
	 * The serial number of the module.
	 */
	@NotNull
	private String serialNo;
	/**
	 * When the module was built.
	 */
	@Past
	private LocalDate manufactureDate;
	/**
	 * The version of the communication specification used by the module.
	 */
	private String commSpecVersion;
	/**
	 * The number of storage blocks the module has.
	 */
	@Min(0)
	private int numBlocks;
}
