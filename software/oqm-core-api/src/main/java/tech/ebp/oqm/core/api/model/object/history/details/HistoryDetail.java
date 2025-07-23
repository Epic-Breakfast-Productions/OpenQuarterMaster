package tech.ebp.oqm.core.api.model.object.history.details;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

@Data
//@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@BsonDiscriminator
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = FieldsAffectedHistoryDetail.class, name = "FIELDS_AFFECTED"),
	@JsonSubTypes.Type(value = ItemTransactionDetail.class, name = "ITEM_TRANSACTION"),
	@JsonSubTypes.Type(value = NoteHistoryDetail.class, name = "NOTE"),
	@JsonSubTypes.Type(value = FromSchemaUpgradeDetail.class, name = "FROM_SCHEMA_UPGRADE"),
})
public abstract class HistoryDetail {

	@ToString.Include
	public abstract HistoryDetailType getType();
}
