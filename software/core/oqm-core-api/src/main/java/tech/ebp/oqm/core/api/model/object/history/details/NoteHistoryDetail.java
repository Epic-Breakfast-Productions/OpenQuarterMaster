package tech.ebp.oqm.core.api.model.object.history.details;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@BsonDiscriminator
@Schema(title = "NoteHistoryDetail", description = "A Note attached to the event.")
public class NoteHistoryDetail extends HistoryDetail {

	@Schema(description = "The note that was attached to the event.")
	private String note;

	@Override
	@Schema(constValue = "NOTE", readOnly = true, required = true, examples = "NOTE")
	public HistoryDetailType getType() {
		return HistoryDetailType.NOTE;
	}
}
