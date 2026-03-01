const ItemCheckin = {
	modal: $("#itemCheckinModal"),
	modalBs: new bootstrap.Modal($("#itemCheckinModal"), {}),
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
		ItemCheckin.returnedDtInput.val(TimeUtils.getNowTs());
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

		await Rest.call({
			spinnerContainer: ItemCheckin.modal.get(0),
			url: Rest.passRoot + "/inventory/item-checkout/" + checkoutId,
			method: "GET",
			async: false,
			done: async function (data) {

				if (!data.stillCheckedOut) {
					ItemCheckin.modalBs.hide();
					ItemCheckoutView.viewBsModal.show();
					await ItemCheckoutView.setupView(checkoutId);
					//TODO:: close checkin view
					PageMessageUtils.addMessageToDiv(
						ItemCheckoutView.messages,
						"danger",
						"This has already been checked in. Details below.",
						"Already checked in!"
					);
					return;
				}

				let promises = [];

				promises.push(Rest.call({
					url: Rest.passRoot + "/inventory/item/" + data.item,
					method: "GET",
					async: false,
					failMessagesDiv: ItemCheckin.messages,
					done: function (itemData) {
						ItemCheckin.itemNameLabel.text(itemData.name);

						for (const storageId of Object.keys(itemData.storageMap)) {
							console.log("hello " + storageId);
							getStorageBlockLabel(storageId, function (label) {
								let newOp = $("<option></option>")
								newOp.attr("value", storageId);
								newOp.text(label);
								if (storageId === data.checkedOutFrom) {
									newOp.prop("selected", true);
								}
								ItemCheckin.intoInput.append(newOp);
							});
						}
					}
				}));

				getStorageBlockLabel(data.checkedOutFrom, function (label) {
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
		"checkinDateTime": TimeUtils.getTsFromInput(ItemCheckin.returnedDtInput),
		"notes": ItemCheckin.notesInput.val(),
		"checkinType": ItemCheckin.checkinTypeInput.val()
	};
	KeywordAttEdit.addKeywordAttData(checkinDetailsData, ItemCheckin.keywords, ItemCheckin.atts);

	switch (checkinDetailsData.checkinType){
		case "RETURN":
			checkinDetailsData["storageBlockCheckedInto"]=ItemCheckin.intoInput.val();
			break;
		case "LOSS":
			checkinDetailsData["reason"]=ItemCheckin.lossReasonInput.val();
			break;
		default:
			PageMessageUtils.addMessageToDiv(
				ItemCheckin.messages,
				"danger",
				"Invalid checkin type"
			);
			return;
	}

	Rest.call({
		spinnerContainer: ItemCheckin.modal.get(0),
		failMessagesDiv: ItemCheckin.messages,
		url: Rest.passRoot + "/inventory/item-checkout/" + ItemCheckin.checkinIdInput.val() + "/checkin",
		method: "PUT",
		data: checkinDetailsData,
		done: function (data) {
			if (UriUtils.getParams.has("checkin")) {
				UriUtils.removeParam("checkin");
			}
			PageMessageUtils.reloadPageWithMessage("Checked in item successfully!", "success", "Success!");
		}
	})
});

ItemCheckin.modal[0].addEventListener("hidden.bs.modal", function () {
	UriUtils.removeParam("checkin");
});

if (UriUtils.getParams.has("checkin")) {
	ItemCheckin.setupCheckinForm(UriUtils.getParams.get("checkin"));
	ItemCheckin.modalBs.show();
}