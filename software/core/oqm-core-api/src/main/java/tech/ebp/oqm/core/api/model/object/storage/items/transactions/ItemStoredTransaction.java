package tech.ebp.oqm.core.api.model.object.storage.items.transactions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import tech.ebp.oqm.core.api.model.object.Versionable;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.general.add.AddAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.general.add.AddWholeTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkouts.checkin.CheckinFullTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkouts.checkin.CheckinLossTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkouts.checkin.CheckinPartTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkouts.checkout.CheckoutAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkouts.checkout.CheckoutWholeTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.add.AddAmountInTransitTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.add.AddWholeInTransitTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.cancel.CancelInTransitWholeTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.receive.ReceiveAmountInTransitTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.receive.ReceiveWholeInTransitTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.storedToInTransit.StoredToInTransitAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.storedToInTransit.StoredToInTransitWholeTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.update.UpdateInTransitTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.general.set.SetAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.general.subtract.SubAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.general.subtract.SubWholeTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.general.transfer.TransferAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.general.transfer.TransferWholeTransaction;

/**
 * This class is the superclass for all transaction objects.
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type"
)
@JsonSubTypes(value = {
	@JsonSubTypes.Type(value = AddAmountTransaction.class, name = "ADD_AMOUNT"),
	@JsonSubTypes.Type(value = AddWholeTransaction.class, name = "ADD_WHOLE"),
	@JsonSubTypes.Type(value = CheckinPartTransaction.class, name = "CHECKIN_PART"),
	@JsonSubTypes.Type(value = CheckinFullTransaction.class, name = "CHECKIN_FULL"),
	@JsonSubTypes.Type(value = CheckinLossTransaction.class, name = "CHECKIN_LOSS"),
	@JsonSubTypes.Type(value = CheckoutAmountTransaction.class, name = "CHECKOUT_AMOUNT"),
	@JsonSubTypes.Type(value = CheckoutWholeTransaction.class, name = "CHECKOUT_WHOLE"),
	@JsonSubTypes.Type(value = SetAmountTransaction.class, name = "SET_AMOUNT"),
	@JsonSubTypes.Type(value = SubAmountTransaction.class, name = "SUBTRACT_AMOUNT"),
	@JsonSubTypes.Type(value = SubWholeTransaction.class, name = "SUBTRACT_WHOLE"),
	@JsonSubTypes.Type(value = TransferAmountTransaction.class, name = "TRANSFER_AMOUNT"),
	@JsonSubTypes.Type(value = TransferWholeTransaction.class, name = "TRANSFER_WHOLE"),
	// in-transit
	@JsonSubTypes.Type(value = AddAmountInTransitTransaction.class, name = "ADD_AMOUNT_IN_TRANSIT"),
	@JsonSubTypes.Type(value = AddWholeInTransitTransaction.class, name = "ADD_WHOLE_IN_TRANSIT"),
	@JsonSubTypes.Type(value = CancelInTransitWholeTransaction.class, name = "CANCEL_IN_TRANSIT_WHOLE"),
	@JsonSubTypes.Type(value = ReceiveWholeInTransitTransaction.class, name = "RECEIVE_WHOLE_IN_TRANSIT"),
	@JsonSubTypes.Type(value = ReceiveAmountInTransitTransaction.class, name = "RECEIVE_AMOUNT_IN_TRANSIT"),
	@JsonSubTypes.Type(value = StoredToInTransitAmountTransaction.class, name = "STORED_TO_IN_TRANSIT_AMOUNT"),
	@JsonSubTypes.Type(value = StoredToInTransitWholeTransaction.class, name = "STORED_TO_IN_TRANSIT_WHOLE"),
	@JsonSubTypes.Type(value = UpdateInTransitTransaction.class, name = "UPDATE_IN_TRANSIT"),

})
@JsonInclude(JsonInclude.Include.ALWAYS)
@BsonDiscriminator
@Schema(oneOf = {
	AddAmountTransaction.class,
	AddWholeTransaction.class,
	CheckinPartTransaction.class,
	CheckinFullTransaction.class,
	CheckinLossTransaction.class,
	CheckoutAmountTransaction.class,
	CheckoutWholeTransaction.class,
	SetAmountTransaction.class,
	SubAmountTransaction.class,
	SubWholeTransaction.class,
	TransferAmountTransaction.class,
	TransferWholeTransaction.class,
	//in-transit
	AddAmountInTransitTransaction.class,
	AddWholeInTransitTransaction.class,
	CancelInTransitWholeTransaction.class,
	ReceiveWholeInTransitTransaction.class,
	ReceiveAmountInTransitTransaction.class,
	StoredToInTransitAmountTransaction.class,
	StoredToInTransitWholeTransaction.class,
	UpdateInTransitTransaction.class,
})
public abstract class ItemStoredTransaction implements Versionable {

	/**
	 * The type of transaction.
	 * @return The transaction type.
	 */
	public abstract TransactionType getType();

}
