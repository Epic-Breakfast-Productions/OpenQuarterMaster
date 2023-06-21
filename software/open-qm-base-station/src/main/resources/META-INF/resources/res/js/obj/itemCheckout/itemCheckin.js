const ItemCheckin = {
	modal: $("#itemCheckinModal"),
	messages: $("#itemCheckinMessages"),
	itemNameLabel: $("#itemCheckinItemNameLabel"),
	originalStorageLabelLabel: $("#itemCheckinOriginalStorageLabelLabel"),
	storedDetails: $("#itemCheckinStoredDetails"),
	itemCheckinForm: $("#itemCheckinForm"),

	checkinIdInput: $("#itemCheckinFormCheckinIdInput"),
	returnedDtInput: $("#itemCheckinFormReturnedInput"),
	notesInput: $("#itemCheckinFormNotesInput"),
	checkinTypeInput: $("#itemCheckinFormTypeInput"),
	intoInputContainer: $("#itemCheckinFormIntoInputContainer"),
	intoInput: $("#itemCheckinFormIntoInput"),
	lossReasonInputContainer: $("#itemCheckinFormLossReasonInputContainer"),
	lossReasonInput: $("#itemCheckinFormLossReasonInput"),
	keywords: $("#itemCheckinForm").find(".keywordInputDiv"),
	atts: $("#itemCheckinForm").find(".attInputDiv"),

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
		ItemCheckin.checkinIdInput.val("");
		ItemCheckin.messages.text("");
		ItemCheckin.itemNameLabel.text("");
		ItemCheckin.originalStorageLabelLabel.text("");
		ItemCheckin.storedDetails.text("");
		ItemCheckin.returnedDtInput.val(TimeHelpers.getNowTs());
		ItemCheckin.notesInput.val("");
		ItemCheckin.checkinTypeInput.val("RETURN");
		ItemCheckin.setCheckinTypeView();
		ItemCheckin.intoInput.html("");
		ItemCheckin.lossReasonInput.val("");
	},
	async setupCheckinForm(checkoutId){
		console.log("Checking in " + checkoutId);
		this.resetCheckinForm();

		ItemCheckin.checkinIdInput.val(checkoutId);

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

ItemCheckin.itemCheckinForm.on("submit", function(e){
	e.preventDefault();
	console.log("Submitting item checkin form.");

	let checkinDetailsData = {
		"checkinDateTime": ItemCheckin.returnedDtInput.val(),
		"notes": ItemCheckin.notesInput.val(),
		"checkinType": ItemCheckin.checkinTypeInput.val()
	};
	addKeywordAttData(checkinDetailsData, ItemCheckin.keywords, ItemCheckin.atts);

	switch (checkinDetailsData.checkinType){
		case "RETURN":
			checkinDetailsData["storageBlockCheckedInto"]=ItemCheckin.intoInput.val();
			break;
		case "LOSS":
			checkinDetailsData["reason"]=ItemCheckin.lossReasonInput.val();
			break;
		default:
			PageMessages.addMessageToDiv(
				ItemCheckin.messages,
				"danger",
				"Invalid checkin type"
			);
			return;
	}

	doRestCall({
		spinnerContainer: ItemCheckin.modal,
		failMessagesDiv: ItemCheckin.messages,
		url: "/api/v1/inventory/item-checkout/" + ItemCheckin.checkinIdInput.val() + "/checkin",
		method: "PUT",
		data: checkinDetailsData,
		done: function (data) {
			PageMessages.reloadPageWithMessage("Checked in item successfully!", "success", "Success!");
		}
	})



});