package tech.ebp.oqm.lib.core.object.storage.checkout;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.rest.storage.itemCheckout.ItemCheckinRequest;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckInDetails {
	
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
	@NonNull
	@NotNull
	private ObjectId storageBlockCheckedInto;
	
	/**
	 * Notes about the checkin
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private String notes = "";
	
	/**
	 * When the checkin took place
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private ZonedDateTime checkinDateTime = ZonedDateTime.now();
	
	public static CheckInDetails.Builder fromRequest(ItemCheckinRequest request){
		CheckInDetails.Builder builder = CheckInDetails.builder();
		builder.state(request.getState());
		builder.storageBlockCheckedInto(request.getStorageBlockCheckedInto());
		builder.notes(request.getNotes());
		
		if(request.getCheckinDateTime() != null){
			builder.checkinDateTime(request.getCheckinDateTime());
		}
		
		return builder;
	}
}
