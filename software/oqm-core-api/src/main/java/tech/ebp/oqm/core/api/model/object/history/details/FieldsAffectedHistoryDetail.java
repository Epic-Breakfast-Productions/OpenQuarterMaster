package tech.ebp.oqm.core.api.model.object.history.details;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FieldsAffectedHistoryDetail extends HistoryDetail {

	private String note;

	@Override
	public HistoryDetailType getType() {
		return HistoryDetailType.FIELDS_AFFECTED;
	}
}
