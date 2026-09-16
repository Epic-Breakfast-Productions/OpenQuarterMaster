package tech.ebp.oqm.core.baseStation.model.printouts;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.ToString;

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
