package tech.ebp.oqm.core.baseStation.model.objReference;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ObjectReference {

	@NotNull
	@NonNull
	private ObjRefObjType type;

	@NotNull
	@NonNull
	private String id;

	private String id2;
}
