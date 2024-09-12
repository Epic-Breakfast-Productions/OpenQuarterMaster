package tech.ebp.oqm.core.api.model.object.history.details;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@BsonDiscriminator
public abstract class HistoryDetail {

	public abstract HistoryDetailType getType();
}
