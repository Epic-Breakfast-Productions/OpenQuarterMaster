package tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@ToString
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public abstract class SearchObject {
	//paging
	@QueryParam("pageSize") Integer pageSize;
	@QueryParam("pageNum") Integer pageNum;
	//sorting
	@QueryParam("sortBy") String sortField;
	@QueryParam("sortType") String sortType;
	
}
