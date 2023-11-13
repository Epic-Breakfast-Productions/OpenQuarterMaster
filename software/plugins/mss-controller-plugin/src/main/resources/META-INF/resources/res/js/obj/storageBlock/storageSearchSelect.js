
const StorageSearchSelect = {
	storageSearchSelectModal: $("#storageSearchSelectModal"),
	storageSearchSelectForm: $("#storageSearchSelectForm"),
	storageSearchSelectResults: $("#storageSearchSelectResults"),
	setupStorageSearchModal(inputIdPrepend) {
		StorageSearchSelect.storageSearchSelectModal.attr("data-bs-inputIdPrepend", inputIdPrepend);
	},
	selectStorageBlock(blockName, blockId, inputIdPrepend, otherModalId) {
		let nameInputId = inputIdPrepend + "Id";
		let nameInputName = inputIdPrepend + "Name";

		$("#" + nameInputId).val(blockId);
		$("#" + nameInputName).val(blockName);
	}
};

StorageSearchSelect.storageSearchSelectForm.on("submit", function (event) {
	event.preventDefault();
	console.log("Submitting storage block search form.");

	let searchParams = new URLSearchParams(new FormData(event.target));
	console.log("URL search params: " + searchParams);

	doRestCall({
		spinnerContainer: StorageSearchSelect.storageSearchSelectModal.get(0),
		url: "/api/v1/inventory/storage-block?" + searchParams,
		method: 'GET',
		failNoResponse: null,
		failNoResponseCheckStatus: true,
		extraHeaders: {
			"accept": "text/html",
			"actionType": "select",
			"searchFormId": "storageSearchSelectForm",
			"inputIdPrepend": StorageSearchSelect.storageSearchSelectModal.attr("data-bs-inputIdPrepend"),
			"otherModalId": StorageSearchSelect.storageSearchSelectModal.attr("data-bs-otherModalId")
		},
		async: false,
		done: function (data) {
			console.log("Got data!");
			StorageSearchSelect.storageSearchSelectResults.html(data);
		}
	});
});
