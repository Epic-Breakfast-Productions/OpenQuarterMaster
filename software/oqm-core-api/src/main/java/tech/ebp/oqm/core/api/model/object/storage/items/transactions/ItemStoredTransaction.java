package tech.ebp.oqm.core.api.model.object.storage.items.transactions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import tech.ebp.oqm.core.api.model.object.Versionable;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.add.AddAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.add.AddWholeTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkin.CheckinPartTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkin.CheckinFullTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkout.CheckoutAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkout.CheckoutWholeTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.subtract.SubAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.subtract.SubWholeTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.transfer.TransferAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.transfer.TransferWholeTransaction;

@Data
@SuperBuilder(toBuilder = true)
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "transactionType"
)
@JsonSubTypes(value = {
	@JsonSubTypes.Type(value = AddAmountTransaction.class, name = "ADD_AMOUNT"),
	@JsonSubTypes.Type(value = AddWholeTransaction.class, name = "ADD_WHOLE"),
	@JsonSubTypes.Type(value = CheckinPartTransaction.class, name = "CHECKIN_PART"),
	@JsonSubTypes.Type(value = CheckinFullTransaction.class, name = "CHECKIN_FULL"),
	@JsonSubTypes.Type(value = CheckoutAmountTransaction.class, name = "CHECKOUT_AMOUNT"),
	@JsonSubTypes.Type(value = CheckoutWholeTransaction.class, name = "CHECKOUT_WHOLE"),
	@JsonSubTypes.Type(value = SubAmountTransaction.class, name = "SUBTRACT_AMOUNT"),
	@JsonSubTypes.Type(value = SubWholeTransaction.class, name = "SUBTRACT_WHOLE"),
	@JsonSubTypes.Type(value = TransferAmountTransaction.class, name = "TRANSFER_AMOUNT"),
	@JsonSubTypes.Type(value = TransferWholeTransaction.class, name = "TRANSFER_WHOLE"),
})
@JsonInclude(JsonInclude.Include.ALWAYS)
@BsonDiscriminator
public abstract class ItemStoredTransaction implements Versionable {

	public abstract TransactionType getTransactionType();

}
