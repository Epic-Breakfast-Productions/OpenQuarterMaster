package tech.ebp.oqm.core.api.model.object.history.details;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
@BsonDiscriminator
public class FieldsAffectedHistoryDetail extends HistoryDetail {
	private static List<String> getFields(ObjectNode updates) {
		List<String> fields = new ArrayList<>();

		Iterator<Map.Entry<String, JsonNode>> fieldIterator = updates.fields();

		while (fieldIterator.hasNext()) {
			Map.Entry<String, JsonNode> entry = fieldIterator.next();
			String field = entry.getKey();
			JsonNode value = entry.getValue();

			List<String> subFields = getFields(value);
			if(subFields.isEmpty()) {
				fields.add(field);
			} else {
				fields.addAll(
					subFields.stream()
						.map((cur) -> {
							if(cur.startsWith("[")){
								return field + cur;
							} else {
								return field + "." + cur;
							}
						})
						.collect(Collectors.toSet())
				);
			}
		}
		return fields;
	}

	private static List<String> getFields(ArrayNode updates) {
		List<String> fields = new ArrayList<>();

		for (int i = 0; i < updates.size(); i++) {
			JsonNode curArrayElement = updates.get(i);
			List<String> subFields = getFields(curArrayElement);

			int finalI = i;
			fields.addAll(
				subFields.stream()
					.map((cur) -> {
						String index = "[" + finalI + "]";
						return index + cur;
					})
					.collect(Collectors.toSet())
			);
		}
		return fields;
	}

	public static List<String> getFields(JsonNode updates) {
		if (updates.isObject()) {
			return getFields((ObjectNode) updates);
		} else if (updates.isArray()) {
			return getFields((ArrayNode) updates);
		}
		return List.of();
	}

	@NonNull
	@NotNull
	@lombok.Builder.Default
	private List<String> fieldsUpdated = new ArrayList<>();

	@Override
	public HistoryDetailType getType() {
		return HistoryDetailType.FIELDS_AFFECTED;
	}
}
