package tech.ebp.oqm.core.api.model.object.history.details;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@BsonDiscriminator
public class FromSchemaUpgradeDetail extends HistoryDetail {
	
	@NonNull
	private String upgradeId;
	@NonNull
	private String objectCreatedFromType;
	@NonNull
	private ObjectId objectCreatedFrom;
	@NonNull
	private ObjectId schemaUpgradeEvent;

	@Override
	public HistoryDetailType getType() {
		return HistoryDetailType.FROM_SCHEMA_UPGRADE;
	}
}
