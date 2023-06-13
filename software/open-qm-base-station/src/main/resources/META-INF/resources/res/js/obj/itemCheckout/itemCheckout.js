
const ItemCheckout = {
	itemCheckoutModal: $("#itemCheckoutModal"),
	messages: $("#itemCheckoutMessages"),
	itemNameLabel: $("#itemCheckoutItemNameLabel"),
	storageFromLabel: $("#itemCheckoutStorageLabelLabel"),

	itemCheckoutForm: $("#itemCheckoutForm"),
	itemIdInput: $("#itemCheckoutFormItemIdInput"),
	storageFromIdInput: $("#itemCheckoutFormCheckedOutFromIdInput"),
	dueBackInput: $("#itemCheckoutFormDueBackInput"),
	forSelectInput: $('#itemCheckoutFormForSelectInput'),
	forAnotherOqmUserContainer: $('#itemCheckoutFormForAnotherOqmUserContainer'),
	forExternalUserContainer: $('#itemCheckoutFormForExternalUserContainer'),
	forExternalUserNameInput: $("#itemCheckoutFormForExternalUserNameInput"),
	forExternalUserIdInput: $("#itemCheckoutFormForExternalUserIdInput"),
	reasonInput: $("#itemCheckoutFormReasonInput"),
	notesInput: $("#itemCheckoutFormNotesInput"),

	setForView(){
		console.log("Setting checkout for view. Value: " + this.forSelectInput.val())
		this.forAnotherOqmUserContainer.hide();
		this.forExternalUserContainer.hide();
		switch (this.forSelectInput.val()){
			case "otherOqmUser":
				this.forAnotherOqmUserContainer.show();
				break;
			case "extUser":
				this.forExternalUserContainer.show();
				break;
		}
	},
	resetItemCheckoutForm(){
		this.itemNameLabel.text("");
		this.storageFromLabel.text("");
		this.messages.text('');
		this.itemIdInput.val("");
		this.dueBackInput.val("");
		this.forSelectInput.val("self");
		this.setForView();
		this.forExternalUserNameInput.val("");
		this.forExternalUserIdInput.val("");
		this.reasonInput.val("");
		this.notesInput.val("");
	},
	setupCheckoutItemModal(stored, itemId, storageId){
		console.log("Setting up item checkout form for stored: " + JSON.stringify(stored) + " - from Item " + itemId + "/"+storageId);
		this.resetItemCheckoutForm();

		Getters.InventoryItem.getItemName(itemId, function (name){
			ItemCheckout.itemNameLabel.text(name);
		});
		getStorageBlockLabel(storageId, function (label){
			ItemCheckout.storageFromLabel.text(label);
		});

		this.itemIdInput.val(itemId);
		this.storageFromIdInput.val(storageId);

		//TODO:: fill out stored details

	}
};

ItemCheckout.itemCheckoutForm.submit(async function (event) {
	event.preventDefault();
	console.info("Submitting checkout request.");
});