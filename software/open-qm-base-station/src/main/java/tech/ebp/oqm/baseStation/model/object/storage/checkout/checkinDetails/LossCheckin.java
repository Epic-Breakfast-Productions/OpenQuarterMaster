package tech.ebp.oqm.baseStation.model.object.storage.checkout.checkinDetails;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

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
