package tech.ebp.oqm.core.baseStation.model.objReference;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Optional;

@ToString
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ObjRefOptions {

	@NotNull
	@QueryParam("objType")
	ObjRefObjType objType;

	@NotNull
	@QueryParam("id")
	String id;

	@QueryParam("id2")
	Optional<String> id2;

	@QueryParam("includeOqm")
	@DefaultValue("true")
	Boolean includeOqm = true;

}
