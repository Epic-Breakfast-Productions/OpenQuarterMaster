package tech.ebp.oqm.core.api.model.rest.dataImportExport;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter(AccessLevel.PROTECTED)
public class EntityImportResult {

	@lombok.Builder.Default
	private long num = 0;
	@lombok.Builder.Default
	private Map<ObjectId, ObjectId> interactingEntitiesMapped = new HashMap<>();
}
