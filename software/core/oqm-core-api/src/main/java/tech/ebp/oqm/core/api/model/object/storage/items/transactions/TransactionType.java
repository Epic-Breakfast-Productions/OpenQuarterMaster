package tech.ebp.oqm.core.api.model.object.storage.items.transactions;

public enum TransactionType {
	ADD_AMOUNT,
	ADD_WHOLE,

	CHECKIN_FULL,
	CHECKIN_LOSS,
	CHECKIN_PART,
	CHECKOUT_AMOUNT,
	CHECKOUT_WHOLE,

	SET_AMOUNT,

	SUBTRACT_AMOUNT,
	SUBTRACT_WHOLE,

	TRANSFER_AMOUNT,
	TRANSFER_WHOLE;
}
