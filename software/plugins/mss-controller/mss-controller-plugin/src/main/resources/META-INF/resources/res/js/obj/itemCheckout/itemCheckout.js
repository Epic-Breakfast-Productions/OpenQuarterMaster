const ItemCheckout = {
	itemCheckoutModal: $("#itemCheckoutModal"),
	messages: $("#itemCheckoutMessages"),
	itemNameLabel: $("#itemCheckoutItemNameLabel"),
	storageFromLabel: $("#itemCheckoutStorageLabelLabel"),
	storedDetails: $("#itemCheckoutStoredDetails"),

	itemCheckoutForm: $("#itemCheckoutForm"),
	itemIdInput: $("#itemCheckoutFormItemIdInput"),
	storageFromIdInput: $("#itemCheckoutFormCheckedOutFromIdInput"),
	toCheckoutInput: $("#itemCheckoutFormToCheckoutInput"),
	dueBackInput: $("#itemCheckoutFormDueBackInput"),
	forSelectInput: $('#itemCheckoutFormForSelectInput'),
	forAnotherOqmUserContainer: $('#itemCheckoutFormForAnotherOqmUserContainer'),
	forExternalUserContainer: $('#itemCheckoutFormForExternalUserContainer'),
	forExternalUserNameInput: $("#itemCheckoutFormForExternalUserNameInput"),
	forExternalUserIdInput: $("#itemCheckoutFormForExternalUserIdInput"),
	reasonInput: $("#itemCheckoutFormReasonInput"),
	notesInput: $("#itemCheckoutFormNotesInput"),
	keywords: $("#itemCheckoutModal").find(".keywordInputDiv"),
	atts: $("#itemCheckoutModal").find(".attInputDiv"),

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
		this.storedDetails.text("");
		this.messages.text('');
		this.itemIdInput.val("");
		this.toCheckoutInput.val("");
		this.dueBackInput.val("");
		this.forSelectInput.val("self");
		this.setForView();
		this.forExternalUserNameInput.val("");
		this.forExternalUserIdInput.val("");
		this.reasonInput.val("");
		this.notesInput.val("");
		this.keywords.text("");
		this.atts.text("");
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
		this.toCheckoutInput.val(JSON.stringify(stored));

		this.storedDetails.append(
			StoredView.getStoredViewContent(
				stored,
				itemId,
				storageId,
				false,
				false,
				false,
				true
			)
		);
	}
};

ItemCheckout.itemCheckoutForm.submit(async function (event) {
	event.preventDefault();
	console.info("Submitting checkout request.");

	let checkoutRequestData = {
		"item": ItemCheckout.itemIdInput.val(),
		"checkedOutFrom": ItemCheckout.storageFromIdInput.val(),
		"toCheckout": JSON.parse(ItemCheckout.toCheckoutInput.val()),
		"checkedOutFor": null,
		"dueBack": ItemCheckout.dueBackInput.val(),
		"reason": ItemCheckout.reasonInput.val(),
		"notes": ItemCheckout.notesInput.val()
	}
	addKeywordAttData(checkoutRequestData, ItemCheckout.keywords, ItemCheckout.atts);

	//TODO:: checkedOutFor
	//TODO:: adjusting the amount checked out for AMOUNT_STORED

	await doRestCall({
		url: "/api/v1/inventory/item-checkout",
		method: "POST",
		data: checkoutRequestData,
		async: false,
		done: function (data) {
			//TODO:: send to more convenient location
			PageMessages.reloadPageWithMessage("Checked out item successfully!", "success", "Success!");
		},
		failMessagesDiv: ItemCheckout.messages
	});
});