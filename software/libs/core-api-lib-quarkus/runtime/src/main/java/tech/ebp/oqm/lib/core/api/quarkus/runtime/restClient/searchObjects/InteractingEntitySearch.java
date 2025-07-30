package tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString(callSuper = true)
@Getter
public class InteractingEntitySearch extends SearchObject {
	@QueryParam("name") String name;
	@QueryParam("type") List<String> types;
}
