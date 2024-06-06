package tech.ebp.oqm.core.api.rest.dataImportExport;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.Map;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Setter(AccessLevel.PROTECTED)
public class DataImportResult {
	
	@lombok.Builder.Default
	private long numUnits = 0;

	@lombok.Builder.Default
	private EntityImportResult entities = new EntityImportResult();

	@lombok.Builder.Default
	private Map<String, DbImportResult> dbResults = new HashMap<>();
}
