package tech.ebp.oqm.baseStation.rest.printouts;

import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import javax.ws.rs.QueryParam;

@ToString
@Getter
public class InventorySheetsOptions {
	
	@NotNull
	@QueryParam("pageSize")
	PageSizeOption pageSize = PageSizeOption.DEFAULT;
	
	@NotNull
	@QueryParam("orientation") PageOrientation pageOrientation = PageOrientation.LANDSCAPE;
	
//	TODO:: flags to include/exclude fields, include att/keywords
}
