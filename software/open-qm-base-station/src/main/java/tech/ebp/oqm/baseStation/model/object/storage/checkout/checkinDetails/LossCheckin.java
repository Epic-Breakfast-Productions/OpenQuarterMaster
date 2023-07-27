package tech.ebp.oqm.baseStation.model.object.storage.checkout.checkinDetails;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import tech.ebp.oqm.baseStation.model.object.storage.checkout.checkinDetails.CheckInDetails;
import tech.ebp.oqm.baseStation.model.object.storage.checkout.checkinDetails.CheckInType;

import javax.validation.constraints.NotNull;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LossCheckin extends CheckInDetails {
	
	@NonNull
	@NotNull
	public String reason;
	
	@Override
	public CheckInType getCheckinType() {
		return CheckInType.LOSS;
	}
}
