const ItemCheckin = {
	messages: $("#itemCheckinMessages"),
	itemNameLabel: $("#itemCheckinItemNameLabel"),
	originalStorageLabelLabel: $("#itemCheckinOriginalStorageLabelLabel"),
	storedDetails: $("#itemCheckinStoredDetails"),
	returnedDtInput: $("#itemCheckinFormReturnedInput"),


	resetCheckinForm(){
		ItemCheckin.messages.text("");
		ItemCheckin.itemNameLabel.text("");
		ItemCheckin.originalStorageLabelLabel.text("");
		ItemCheckin.storedDetails.text("");
		ItemCheckin.returnedDtInput.val(new Date().toISOString().slice(0, 16))
	},
	async setupCheckinForm(checkoutId){
		console.log("Checking in " + checkoutId);
		this.resetCheckinForm();

		await doRestCall({

			url: "/api/v1/inventory/item-checkout/" + checkoutId,
			method: "GET",
			async: false,
			done: function (data) {
				Getters.InventoryItem.getItemName(data.item, function (name){
					ItemCheckin.itemNameLabel.text(name);
				});
				getStorageBlockLabel(data.checkedOutFrom, function (label){
					ItemCheckin.originalStorageLabelLabel.text(label);
				});
				ItemCheckin.storedDetails.append(
					StoredView.getStoredViewContent(
						data.checkedOut,
						data.item,
						data.checkedOutFrom,
						false,
						false,
						false,
						true
					)
				);
			},
			failMessagesDiv: ItemCheckin.messages
		});
	}
};