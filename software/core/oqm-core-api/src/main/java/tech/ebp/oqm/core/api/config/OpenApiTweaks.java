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
@OpenApiFilter(stages = {OpenApiFilter.RunStage.BUILD, OpenApiFilter.RunStage.RUNTIME_STARTUP, OpenApiFilter.RunStage.RUNTIME_PER_REQUEST})
public class OpenApiTweaks implements OASFilter {
	
	private static Map<String, Schema> getSchemaOverrides(Map<String, Schema> existingSchemas) {
		return new HashMap<>() {{
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
			{// Unit
				Schema unitSchema = OASFactory.createSchema();
				unitSchema.setTitle("Unit");
				unitSchema.setComment("Unit of measurement");
				unitSchema.setType(List.of(Schema.SchemaType.OBJECT));
				
				{
					Schema nameSchema = OASFactory.createSchema();
					nameSchema.type(List.of(Schema.SchemaType.STRING));
					nameSchema.setReadOnly(true);
					nameSchema.description("Name of the unit in 'normal' terms. Derived, provided for downstream usage.");
					nameSchema.setExamples(List.of("Units"));
					
					unitSchema.addProperty("name", nameSchema);
				}
				{
					Schema symbolSchema = OASFactory.createSchema();
					symbolSchema.type(List.of(Schema.SchemaType.STRING));
					symbolSchema.setReadOnly(true);
					symbolSchema.description("The symbol of the unit, as in 5{symbol}. Derived, provided for downstream usage.");
					symbolSchema.setExamples(List.of("units"));
					
					unitSchema.addProperty("symbol", symbolSchema);
				}
				{
					Schema stringSchema = OASFactory.createSchema();
					stringSchema.type(List.of(Schema.SchemaType.STRING));
					stringSchema.description(
						"The string representation of the unit, used in de/serialization. Is the only required field, the others are derived from the deserialized "
						+ "unit.");
					stringSchema.setExamples(List.of("units"));
					unitSchema.addProperty("string", stringSchema);
				}
				unitSchema.setRequired(List.of("string"));
				put("UnitObject", unitSchema); //when Unit<?>
				put("UnitQuantity", unitSchema); //when Unit<?>?
				put("Unit", unitSchema); //when Unit
			}
			{
				Schema monetaryAmountSchema = OASFactory.createSchema();
				monetaryAmountSchema.setComment("An amount of currency");
				monetaryAmountSchema.setType(List.of(Schema.SchemaType.OBJECT));
				
				{
					Schema numberSchema = OASFactory.createSchema();
					numberSchema.type(List.of(Schema.SchemaType.NUMBER));
					numberSchema.description("The amount of the currency..");
					numberSchema.setExamples(List.of("0.00", "0", "5"));
					
					monetaryAmountSchema.addProperty("number", numberSchema);
				}
				
				monetaryAmountSchema.addProperty("currency", this.get("Currency"));// ..don't know how this works just that it does?
				
				monetaryAmountSchema.setRequired(List.of("currency", "number"));
				put("MonetaryAmount", monetaryAmountSchema);
			}
		}};
	}
	
	@Override
	public void filterOpenAPI(OpenAPI openAPI) {
		log.info("Running OpenApiTweaks filter");
		
		Map<String, Schema> schemas = new HashMap<>(openAPI.getComponents().getSchemas());
		schemas.putAll(getSchemaOverrides(schemas));
		openAPI.getComponents().setSchemas(schemas);
		
		log.debug("Done OpenApiTweaks filter");
	}
	
}
