package com.ebp.openQuarterMaster.lib.driver;

import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.time.ZonedDateTime;

@Data
public class ModuleInteractionInfo {
	
	@NonNull
	@NotNull
	private ModuleInfo info;
	@NonNull
	@NotNull
	private ModuleState state;
	
	@NonNull
	@NotNull
	@Past
	private ZonedDateTime firstFound;
	
	@NonNull
	@NotNull
	@Past
	private ZonedDateTime lastComm;
	
}
