package tech.ebp.oqm.core.api.model.object.history.details;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NoteHistoryDetail extends HistoryDetail {

	private String note;

	@Override
	public HistoryDetailType getType() {
		return HistoryDetailType.NOTE;
	}
}
