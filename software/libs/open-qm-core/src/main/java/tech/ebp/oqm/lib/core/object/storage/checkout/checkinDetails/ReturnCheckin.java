package tech.ebp.oqm.lib.core.object.storage.checkout.checkinDetails;

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
	public CheckInType getCheckinType() {
		return CheckInType.RETURN;
	}
}
