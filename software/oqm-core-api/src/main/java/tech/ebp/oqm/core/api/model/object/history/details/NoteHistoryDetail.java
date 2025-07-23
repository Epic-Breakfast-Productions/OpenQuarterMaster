package tech.ebp.oqm.core.api.model.object.history.details;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@BsonDiscriminator
public class NoteHistoryDetail extends HistoryDetail {

	private String note;

	@Override
	public HistoryDetailType getType() {
		return HistoryDetailType.NOTE;
	}
}
