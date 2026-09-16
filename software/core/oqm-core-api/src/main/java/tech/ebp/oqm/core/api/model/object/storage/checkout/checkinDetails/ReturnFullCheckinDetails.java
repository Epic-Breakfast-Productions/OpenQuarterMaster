package tech.ebp.oqm.core.api.model.object.storage.checkout.checkinDetails;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.core.api.model.object.storage.checkout.CheckInType;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
//@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class ReturnFullCheckinDetails extends CheckInDetails {
	
	@Override
	public CheckInType getType() {
		return CheckInType.RETURN_FULL;
	}
}
