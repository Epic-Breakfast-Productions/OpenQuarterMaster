import {Rest} from "../../Rest.js";

export const StorageSearchSelect = {
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
	},
	initPage: function () {
		console.log("Initializing storage block search select.");
		StorageSearchSelect.storageSearchSelectForm.on("submit", function (event) {
			event.preventDefault();
			Main.processStart();
			console.log("Submitting storage block search form.");

			let searchParams = new URLSearchParams(new FormData(event.target));
			console.log("URL search params: " + searchParams);

			Rest.call({
				spinnerContainer: StorageSearchSelect.storageSearchSelectModal.get(0),
				url: Rest.passRoot + "/inventory/storage-block?" + searchParams,
				method: 'GET',
				failNoResponse: null,
				failNoResponseCheckStatus: true,
				returnType: "html",
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
					Main.processStop();
				}
			});
		});
		console.log("Done initializing storage block search select.");
	}
};


