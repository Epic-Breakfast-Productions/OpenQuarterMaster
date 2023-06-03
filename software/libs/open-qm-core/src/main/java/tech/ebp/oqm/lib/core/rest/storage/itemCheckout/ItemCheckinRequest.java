package tech.ebp.oqm.lib.core.rest.storage.itemCheckout;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.object.storage.checkout.CheckInState;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemCheckinRequest {
	/**
	 * The state of the item being checked in
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private CheckInState state = CheckInState.OK;
	
	/**
	 * Where the item went when checked in
	 */
	@lombok.Builder.Default
	private ObjectId storageBlockCheckedInto = null;
	
	/**
	 * Notes about the checkin
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private String notes = "";
	
	/**
	 * When the checkin took place. Null for now
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private ZonedDateTime checkinDateTime = null;
}
