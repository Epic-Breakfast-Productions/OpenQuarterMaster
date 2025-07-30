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
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.add.AddAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.add.AddWholeTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkin.CheckinFullTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkin.CheckinLossTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkin.CheckinPartTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkout.CheckoutAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkout.CheckoutWholeTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.set.SetAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.subtract.SubAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.subtract.SubWholeTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.transfer.TransferAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.transfer.TransferWholeTransaction;

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
	TransferWholeTransaction.class
})
public abstract class ItemStoredTransaction implements Versionable {
	
	/**
	 * The type of transaction.
	 * @return The transaction type.
	 */
	public abstract TransactionType getType();

}
