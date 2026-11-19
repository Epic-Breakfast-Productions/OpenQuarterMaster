package tech.ebp.oqm.core.api.model.object.storage.items.transactions;

public enum TransactionType {
	/*
	 *
	 * "General" transactions; standard inventory things
	 *
	 */
	ADD_AMOUNT,
	ADD_WHOLE,

	SET_AMOUNT,

	SUBTRACT_AMOUNT,
	SUBTRACT_WHOLE,

	TRANSFER_AMOUNT,
	TRANSFER_WHOLE,

	/*
	 *
	 * checkouts
	 *
	 */
	CHECKIN_FULL,
	CHECKIN_LOSS,
	CHECKIN_PART,
	CHECKOUT_AMOUNT,
	CHECKOUT_WHOLE,

	/*
	 * In-transit related
	 */

	ADD_AMOUNT_IN_TRANSIT,
	ADD_WHOLE_IN_TRANSIT,
	CANCEL_IN_TRANSIT_WHOLE,
	RECEIVE_WHOLE_IN_TRANSIT,
	RECEIVE_AMOUNT_IN_TRANSIT,
	STORED_TO_IN_TRANSIT_AMOUNT,
	STORED_TO_IN_TRANSIT_WHOLE,
	UPDATE_IN_TRANSIT,

	;
}
