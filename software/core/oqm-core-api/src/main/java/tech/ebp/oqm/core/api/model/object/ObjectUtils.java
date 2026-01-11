package tech.ebp.oqm.core.api.model.object;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.javax.money.JavaxMoneyModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;
import tech.ebp.oqm.core.api.model.jackson.ColorModule;
import tech.ebp.oqm.core.api.model.jackson.MongoObjectIdModule;
import tech.ebp.oqm.core.api.model.jackson.TempQuantityJacksonModule;
import tech.ebp.oqm.core.api.model.jackson.UnitModule;
import tech.uom.lib.jackson.UnitJacksonModule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public final class ObjectUtils {
	
	/**
	 * A global object mapper that is pre-configured to handle all objects in lib.
	 * <p>
	 * Configured by {@link #setupObjectMapper(ObjectMapper)}.
	 */
	public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
	/**
	 * Modules needed for proper serialization of lib objects. Registered with {@link #OBJECT_MAPPER}
	 */
	@SuppressWarnings("deprecation")
	public static final Module[] MAPPER_MODULES = {
		new BlackbirdModule(),
		new UnitJacksonModule(),
		new JavaTimeModule(),
		new MongoObjectIdModule(),
		new ColorModule(),
		new TempQuantityJacksonModule(),
		new UnitModule(),
		new JavaxMoneyModule().withQuotedDecimalNumbers()
	};
	
	static {
		ObjectUtils.setupObjectMapper(ObjectUtils.OBJECT_MAPPER);
	}
	
	/**
	 * Configures a given object mapper with the proper configuration to handle all lib objects.
	 *
	 * @param mapper The object mapper to set up.
	 */
	public static void setupObjectMapper(ObjectMapper mapper) {
		mapper.registerModules(MAPPER_MODULES);
		
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
		mapper.enable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID);
		
		//set the timezone to this server's.
		mapper.setTimeZone(TimeZone.getDefault());
	}
	
	public static List<String> fieldListFromJson(ObjectNode updateJson) {
		List<String> output = new ArrayList<>();
		
		for (Iterator<Map.Entry<String, JsonNode>> it = updateJson.fields(); it.hasNext(); ) {
			Map.Entry<String, JsonNode> cur = it.next();
			String curKey = cur.getKey();
			
			if (cur.getValue().isObject()) {
				List<String> curSubs = fieldListFromJson((ObjectNode) cur.getValue());
				
				for (String curSubKey : curSubs) {
					output.add(curKey + "." + curSubKey);
				}
			} else {
				output.add(curKey);
			}
		}
		
		return output;
	}
}
