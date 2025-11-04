package tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects;

import jakarta.ws.rs.QueryParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@NoArgsConstructor
@ToString(callSuper = true)
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class StorageBlockSearch extends SearchKeyAttObject {
	
	//for actual queries
	@QueryParam("labelOrNickname")
	String labelOrNickname;
	@QueryParam("location")
	String location;
	@QueryParam("storedCategories")
	List<String> categories;
	@QueryParam("parentLabel")
	List<String> parents;
	//	@QueryParam("stores") List<ObjectId> stores; //TODO: need aggregate?
	@QueryParam("parent")
	String parent; //TODO:
	@lombok.Builder.Default
	@QueryParam("isParent")
	Boolean isParent = false;
	@lombok.Builder.Default
	@QueryParam("isChild")
	Boolean isChild = false;
	@QueryParam("isChildOf")
	String isChildOf;
	//capacities
	@QueryParam("capacity")
	List<Integer> capacities;//TODO
	@QueryParam("unit")
	List<String> units;//TODO
	
}
