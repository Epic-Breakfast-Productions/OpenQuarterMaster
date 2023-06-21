const ItemCheckin = {
	modal: $("#itemCheckinModal"),
	messages: $("#itemCheckinMessages"),
	itemNameLabel: $("#itemCheckinItemNameLabel"),
	originalStorageLabelLabel: $("#itemCheckinOriginalStorageLabelLabel"),
	storedDetails: $("#itemCheckinStoredDetails"),
	returnedDtInput: $("#itemCheckinFormReturnedInput"),
	notesInput: $("#itemCheckinFormNotesInput"),
	checkinTypeInput: $("#itemCheckinFormTypeInput"),
	intoInputContainer: $("#itemCheckinFormIntoInputContainer"),
	intoInput: $("#itemCheckinFormIntoInput"),
	lossReasonInputContainer: $("#itemCheckinFormLossReasonInputContainer"),
	lossReasonInput: $("#itemCheckinFormLossReasonInput"),

	setCheckinTypeView(){
		ItemCheckin.intoInputContainer.hide();
		ItemCheckin.lossReasonInputContainer.hide();

		switch (ItemCheckin.checkinTypeInput.val()){
			case "RETURN":
				ItemCheckin.intoInputContainer.show();
				break;
			case "LOSS":
				ItemCheckin.lossReasonInputContainer.show();
				break;
		}
	},
	resetCheckinForm(){
		ItemCheckin.messages.text("");
		ItemCheckin.itemNameLabel.text("");
		ItemCheckin.originalStorageLabelLabel.text("");
		ItemCheckin.storedDetails.text("");
		ItemCheckin.returnedDtInput.val(new Date().toISOString().slice(0, 16));
		ItemCheckin.notesInput.val("");
		ItemCheckin.checkinTypeInput.val("RETURN");
		ItemCheckin.setCheckinTypeView();
		ItemCheckin.intoInput.html("");
		ItemCheckin.lossReasonInput.val("");
	},
	async setupCheckinForm(checkoutId){
		console.log("Checking in " + checkoutId);
		this.resetCheckinForm();

		await doRestCall({
			spinnerContainer: ItemCheckin.modal,
			url: "/api/v1/inventory/item-checkout/" + checkoutId,
			method: "GET",
			async: false,
			done: function (data) {
				let promises = [];

				promises.push(doRestCall({
					url: "/api/v1/inventory/item/" + data.item,
					method: "GET",
					async: false,
					failMessagesDiv: ItemCheckin.messages,
					done: function (itemData) {
						ItemCheckin.itemNameLabel.text(itemData.name);

						for (const storageId of Object.keys(itemData.storageMap)) {
							console.log("hello "+ storageId);
							getStorageBlockLabel(storageId, function (label){
								let newOp = $("<option></option>")
								newOp.attr("id", storageId);
								newOp.text(label);
								if(storageId === data.checkedOutFrom){
									newOp.prop("selected", true);
								}
								ItemCheckin.intoInput.append(newOp);
							});
						}
					}
				}));

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