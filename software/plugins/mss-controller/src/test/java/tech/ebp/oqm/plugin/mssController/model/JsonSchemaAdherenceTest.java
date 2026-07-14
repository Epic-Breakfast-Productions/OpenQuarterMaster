package tech.ebp.oqm.plugin.mssController.model;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.moduleInfo.Capabilities;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.moduleInfo.ModuleInfo;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static tech.ebp.oqm.plugin.mssController.model.utils.JacksonUtils.OBJECT_MAPPER;

@Slf4j
public class JsonSchemaAdherenceTest {
	private static final Path SCHEMA_PATH = Path.of("../../../hardware/mss/docs/source/_static/jsonSchemas/");

	private static final Map<Class<?>, JsonSchema> SCHEMA_MAP;

	static {
		JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);

		Function<String, JsonSchema> s = (String schemaFile) -> {
			try (InputStream schemaStream = Files.newInputStream(SCHEMA_PATH.resolve(schemaFile))) {
				// Pre-compile the schema for fast validation reuse
				return factory.getSchema(schemaStream);
			} catch (Exception e) {
				throw new RuntimeException("Failed to load JSON schema", e);
			}
		};

		SCHEMA_MAP = Map.of(
			ModuleInfo.class, s.apply("ModuleInfo.json")
			//TODO:: rest
		);
	}


	public static Stream<Arguments> getValidSchemaTests(){
		return Stream.of(
			Arguments.of(ModuleInfo.class, ModuleInfo.builder()
											   .serialId("foo")
											   .specVersion("v1")
											   .firmwareVersion("v1")
											   .manufactureDate("2026-07-13")
											   .numBlocks(64)
											   .capabilities(Capabilities.builder().build())
											   .build())
			//TODO:: rest
		);
	}


	@ParameterizedTest
	@MethodSource("getValidSchemaTests")
	public void schemaAdherenceTest(Class<?> clazz, Object obj){
		log.info("Testing adherence of {} to schema: {}", clazz.getSimpleName(), obj);

		ObjectNode objNode = OBJECT_MAPPER.valueToTree(obj);

		Set<ValidationMessage> assertions = SCHEMA_MAP.get(clazz).validate(objNode);

		if (assertions.isEmpty()) {
			log.info("JSON is valid!");
		} else {
			log.warn("JSON validation failed:");
			for (ValidationMessage error : assertions) {
				log.warn("- " + error.getMessage());
			}
			Assertions.fail("JSON validation failed; " + assertions.size() + " errors.");
		}
	}
}
