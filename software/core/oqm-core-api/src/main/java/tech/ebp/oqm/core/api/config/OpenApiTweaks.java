package tech.ebp.oqm.core.api.config;

import io.quarkus.smallrye.openapi.OpenApiFilter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.media.Schema;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tweaks the openapi schema. Used to hard override certain schemas.
 * <p>
 * <a href="https://quarkus.io/guides/openapi-swaggerui#enhancing-the-openapi-schema-with-filters">Quarkus guide section</a>
 */
@Slf4j
@OpenApiFilter(OpenApiFilter.RunStage.BOTH)
public class OpenApiTweaks implements OASFilter {
	
	private static final Map<String, Schema> SCHEMA_OVERRIDES = new HashMap<>() {{
		{// ObjectId
			Schema objectIdSchema = OASFactory.createSchema();
			objectIdSchema.setComment("Hex string representation of a MongoDB ObjectId");
			objectIdSchema.setType(List.of(Schema.SchemaType.STRING));
			objectIdSchema.setFormat("objectId");
			objectIdSchema.setPattern("^[0-9a-fA-F]{24}$");
			objectIdSchema.setExamples(List.of("null", "5f9222222222222222222222"));
			put("ObjectId", objectIdSchema);
		}
		{// Currency
			Schema objectIdSchema = OASFactory.createSchema();
			objectIdSchema.setComment("Currency code from ISO 4217 / Java Currency");
			objectIdSchema.setType(List.of(Schema.SchemaType.STRING));
			objectIdSchema.setFormat("currency code");
			objectIdSchema.setExamples(List.of("USD"));
			put("Currency", objectIdSchema);
		}
	}};
	
	@Override
	public void filterOpenAPI(OpenAPI openAPI) {
		log.info("Running OpenApiTweaks filter");
		
		Map<String, Schema> schemas = new HashMap<>(openAPI.getComponents().getSchemas());
		schemas.putAll(SCHEMA_OVERRIDES);
		openAPI.getComponents().setSchemas(schemas);
		
		log.debug("Done OpenApiTweaks filter");
	}
	
}
