package tech.ebp.oqm.plugin.extItemSearch.model;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.utils.ItemKind;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.utils.LookupType;

import java.util.List;

@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExtItemSearch {
	
	@Parameter(description = "The kind of item to search for. If empty, will search for any item. If multiple, treated as an 'or'.")
	@QueryParam("kind")
	List<ItemKind> itemKinds;
	
	@Parameter(description = "The brand of item to search for. If empty, will search for any item. If multiple, treated as an 'or'.")
	@QueryParam("brand")
	List<String> itemBrands;
	
	@Parameter(description = "The type of lookup to perform. If empty, will perform a free text search.")
	@QueryParam("lookupType")
	List<LookupType> lookupTypes;
	
	@Parameter(description = "The service(s) to use to search. If empty, will search all available services.")
	@QueryParam("service")
	List<String> services;
	
	
	@NonNull
	@NotNull
	@NotBlank
	@QueryParam("q")
	String search;
}
