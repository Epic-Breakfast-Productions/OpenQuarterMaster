
const ItemCheckout = {
	itemCheckoutModal: $("#itemCheckoutModal"),
	messages: $("#itemCheckoutMessages"),
	setupCheckoutItemModal(stored, itemId, storageId){
		console.log("Setting up item checkout form for stored: " + JSON.stringify(stored) + " - from Item " + itemId + "/"+storageId);

		ItemCheckout.messages.text("Setting up item checkout form for stored: " + JSON.stringify(stored) + " - from Item " + itemId + "/"+storageId);

	}
};