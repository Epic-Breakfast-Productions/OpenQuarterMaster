package tech.ebp.oqm.plugin.extItemSearch.model;


import io.smallrye.config.WithDefault;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.utils.LookupMethod;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.utils.LookupService;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.utils.LookupSource;

import java.util.List;

@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExtItemSearch {
	
	@Parameter(description = "The type of lookup to perform. If empty, will perform a text search.")
	@QueryParam("lookupMethod")
	@DefaultValue("TEXT")
	List<LookupMethod> lookupMethods;
	
	@Parameter(description = "The data source(s) to use to search. If empty, any are used.")
	@QueryParam("service")
	List<LookupService> services;
	
	@Parameter(
		description = "The source(s) to use to search. Distinct from 'services', as some sources can pull from many services. Example, one service might present data from Amazon,"
					  + " and other retailers. If empty, will search all available."
	)
	@QueryParam("source")
	List<LookupSource> sources;
	
	@NonNull
	@NotNull
	@NotBlank
	@QueryParam("q")
	String search;
	
	@QueryParam("keepNotFound")
	@DefaultValue("true")
	boolean keepNotFound;
}
