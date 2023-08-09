package tech.ebp.oqm.baseStation.rest.printouts;

import lombok.Getter;
import lombok.ToString;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.QueryParam;

@ToString
@Getter
public class InventorySheetsOptions {
	
	@NotNull
	@QueryParam("pageSize")
	PageSizeOption pageSize = PageSizeOption.DEFAULT;
	
	@NotNull
	@QueryParam("orientation") PageOrientation pageOrientation = PageOrientation.LANDSCAPE;
	
	@QueryParam("includeNumCol") boolean includeNumCol = true;
	@QueryParam("includeImageCol") boolean includeImageCol = true;
	@QueryParam("includeConditionCol") boolean includeConditionCol = true;
	
	
//	TODO:: flags to include/exclude fields, include att/keywords
}
