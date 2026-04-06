package tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString(callSuper = true)
@Getter
@Setter
public class IdGeneratorSearch extends SearchObject {
	@QueryParam("name")
	String name;
	
	@QueryParam("label")
	String label;
	
	@QueryParam("generatorFor")
	String generatorFor;
	
	@QueryParam("format")
	String format;
}
