package tech.ebp.oqm.core.api.model.object.storage.checkout.checkinDetails;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.core.api.model.object.storage.checkout.CheckInType;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class LossCheckinDetails extends CheckInDetails {
	
	@NonNull
	@NotNull
	public String reason;
	
	@Override
	public CheckInType getCheckinType() {
		return CheckInType.LOSS;
	}
}
