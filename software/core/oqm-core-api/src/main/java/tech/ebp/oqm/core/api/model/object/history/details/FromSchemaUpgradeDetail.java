package tech.ebp.oqm.core.api.model.object.history.details;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@BsonDiscriminator
@Schema(title = "FromSchemaUpgradeDetail", description = "A schema upgrade event that created this object.")
public class FromSchemaUpgradeDetail extends HistoryDetail {
	
	@NonNull
	@Schema(description = "The id of the schema upgrade that created this object.")
	private String upgradeId;
	
	@NonNull
	@Schema(description = "The type of object that was created from the schema upgrade.")
	private String objectCreatedFromType;
	
	@NonNull
	@Schema(description = "The id of the object that was created from the schema upgrade.")
	private ObjectId objectCreatedFrom;
	
	@NonNull
	@Schema(description = "The id of the schema upgrade event that created this object.")
	private ObjectId schemaUpgradeEvent;
	
	@Override
	@Schema(constValue = "FROM_SCHEMA_UPGRADE", readOnly = true, required = true, examples = "FROM_SCHEMA_UPGRADE")
	public HistoryDetailType getType() {
		return HistoryDetailType.FROM_SCHEMA_UPGRADE;
	}
}
