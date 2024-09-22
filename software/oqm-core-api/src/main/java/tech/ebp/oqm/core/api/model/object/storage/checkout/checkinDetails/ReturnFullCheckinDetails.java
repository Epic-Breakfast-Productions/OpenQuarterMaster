package tech.ebp.oqm.core.api.model.object.storage.checkout.checkinDetails;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.checkout.CheckInType;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
//@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class ReturnFullCheckinDetails extends CheckInDetails {
	
	@Override
	public CheckInType getCheckinType() {
		return CheckInType.RETURN_FULL;
	}
}
