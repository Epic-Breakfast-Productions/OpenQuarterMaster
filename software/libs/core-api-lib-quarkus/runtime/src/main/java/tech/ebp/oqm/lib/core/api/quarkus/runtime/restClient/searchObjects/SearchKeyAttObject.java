package tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;

@NoArgsConstructor
@ToString(callSuper = true)
@Setter
@Getter
@SuperBuilder(toBuilder = true)
public abstract class SearchKeyAttObject extends SearchObject {
	//attKeywords
	@QueryParam("keyword")
	List<String> keywords;
	@QueryParam("attributeKey") List<String> attributeKeys;
	@QueryParam("attributeValue") List<String> attributeValues;
	
}
