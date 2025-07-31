package tech.ebp.oqm.core.api.model.object.storage.checkout.checkinDetails;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.core.api.model.object.storage.checkout.CheckInType;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
//@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class ReturnPartCheckinDetails extends CheckInDetails {
	
	@Override
	public CheckInType getType() {
		return CheckInType.RETURN_PART;
	}
}
