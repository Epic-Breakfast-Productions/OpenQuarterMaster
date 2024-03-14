const ItemSearchSelect = {
	itemSearchSelectModal: $("#itemSearchSelectModal"),
	itemSearchSelectForm: $("#itemSearchSelectForm"),
	itemSearchSelectResults: $("#itemSearchSelectResults"),

	selectItem(itemName, itemId, inputIdPrepend, otherModalId) {
		console.log("Selected item: " + itemId + " - " + itemName);
		let nameInputId = inputIdPrepend + "Id";
		let nameInputName = inputIdPrepend + "Name";

		$("#" + nameInputId).val(itemId);
		$("#" + nameInputName).val(itemName);
	},
	setupItemSearchModal(inputIdPrepend) {
		ItemSearchSelect.itemSearchSelectModal.attr("data-bs-inputIdPrepend", inputIdPrepend);
	},
	clearSearchInput(clearButtPushed){
		clearButtPushed.siblings("input[name=itemName]").val("");
		clearButtPushed.siblings("input[name=item]").val("");
	}
};

ItemSearchSelect.itemSearchSelectForm.on("submit", function (event) {
	event.preventDefault();
	console.log("Submitting search form.");

	var searchParams = new URLSearchParams(new FormData(event.target));
	console.log("URL search params: " + searchParams);

	Rest.call({
		spinnerContainer: ItemSearchSelect.itemSearchSelectModal.get(0),
		url: Rest.passRoot + "/inventory/item?" + searchParams,
		method: 'GET',
		failNoResponse: null,
		failNoResponseCheckStatus: true,
		extraHeaders: {
			"accept": "text/html",
			"actionType": "select",
			"searchFormId": "storageSearchSelectForm",
			"inputIdPrepend": ItemSearchSelect.itemSearchSelectModal.attr("data-bs-inputIdPrepend"),
			"otherModalId": ItemSearchSelect.itemSearchSelectModal.attr("data-bs-otherModalId")
		},
		async: false,
		done: function (data) {
			console.log("Got data!");
			ItemSearchSelect.itemSearchSelectResults.html(data);
		}
	});
});
