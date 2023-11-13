package tech.ebp.oqm.baseStation.model.object.storage.checkout.checkinDetails;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.types.ObjectId;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReturnCheckin extends CheckInDetails {
	/**
	 * Where the item went when checked in
	 */
	private ObjectId storageBlockCheckedInto = null;
	
	@Override
	public tech.ebp.oqm.baseStation.model.object.storage.checkout.checkinDetails.CheckInType getCheckinType() {
		return CheckInType.RETURN;
	}
}
