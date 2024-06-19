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
	storedIdInput: $("#itemCheckoutFormStoredIdInput"),
	amountContainer: $("#itemCheckoutAmountContainer"),
	amountEnableCheck: $("#itemCheckoutAmountEnableCheck"),
	amountInputContainer: $("#itemCheckoutAmountInputContainer"),
	amountAmountInput: $("#itemCheckoutAmountAmountInput"),
	amountUnitInput: $("#itemCheckoutAmountUnitInput"),
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
		this.storedIdInput.val("");
		// this.amountContainer.text("");
		this.amountContainer.hide();
		this.amountEnableCheck.prop('checked', false);
		this.partialAmountCheckChanged();
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
	partialAmountCheckChanged(){
		let checked = this.amountEnableCheck.prop("checked");

		if(checked){
			console.log("Specifying partial amount.");
			this.amountInputContainer.show();
		} else {
			console.log("Specifying not partial amount.");
			this.amountInputContainer.hide();
		}
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
		this.storedIdInput.val(stored.id);

		console.log("Stored type: ", stored.storedType);

		if(stored.storedType === "AMOUNT"){
			//TODO:: set max to current amount stored?
			UnitUtils.updateCompatibleUnits(stored.amount.unit.string, this.amountInputContainer);

			this.amountContainer.show();
		} else {
			console.log("Did not need to show amount to checkout form components.");
		}

		this.storedDetails.append(
			StoredView.getStoredViewContent(
				stored,
				itemId,
				storageId,
				false,
				false,
				false,
				true,
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

	if(checkoutRequestData.toCheckout.storedType === "AMOUNT" && ItemCheckout.amountEnableCheck.prop("checked")){
		let newStored = {
			"id": checkoutRequestData.toCheckout.id,
			"storedType": "AMOUNT",
			"amount": UnitUtils.getQuantityObj(
				ItemCheckout.amountAmountInput.val(),
				ItemCheckout.amountUnitInput.val()
			)
		};
		checkoutRequestData.toCheckout = newStored;
	}

	if(ItemCheckout.forSelectInput.val() === "extUser"){
		checkoutRequestData.checkedOutFor = {};
		checkoutRequestData.checkedOutFor.type = "EXT_SYS_USER";
		checkoutRequestData.checkedOutFor.externalId = ItemCheckout.forExternalUserIdInput.val();
		checkoutRequestData.checkedOutFor.name = ItemCheckout.forExternalUserNameInput.val();
	} else if (ItemCheckout.forSelectInput.val() === "otherOqmUser"){
		//TODO:: this
	}


	KeywordAttEdit.addKeywordAttData(checkoutRequestData, ItemCheckout.keywords, ItemCheckout.atts);

	await Rest.call({
		url: Rest.passRoot + "/inventory/item-checkout",
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