import {Rest} from "../../Rest.js";
import {ModalUtils} from "../../ModalUtils.js";
import {Getters} from "../Getters";

export const StorageSearchSelect = {
	storageSearchSelectModal: $("#storageSearchSelectModal"),
	storageSearchSelectForm: $("#storageSearchSelectForm"),
	storageSearchSelectResults: $("#storageSearchSelectResults"),

	setupStorageSearchModal(inputIdPrepend, initialModal) { //TODO:: make play nice with multiple inputs on one page
		StorageSearchSelect.storageSearchSelectModal.attr("data-bs-inputIdPrepend", inputIdPrepend);

		ModalUtils.setReturnModal(StorageSearchSelect.storageSearchSelectModal, initialModal);
	},
	selectStorageBlock(blockName, blockId, inputIdPrepend) {
		let nameInputId = inputIdPrepend + "Id";
		let nameInputName = inputIdPrepend + "Name";

		$("#" + nameInputId).val(blockId);
		$("#" + nameInputName).val(blockName);
	},


	input: {
		clear(inputJq){
			StorageSearchSelect.input.getIdInput(inputJq).val("");
			StorageSearchSelect.input.getNameInput(inputJq).val("");
		},
		getSearchButton(inputJq){
			return inputJq.find("button.searchButton");
		},
		getIdInput(inputJq){
			return inputJq.find("input.idInput");
		},
		getNameInput(inputJq){
			return inputJq.find("input.nameInput");
		},
		getClearButton(inputJq){
			return inputJq.find("button.clearButton");
		},
		setReadonly(inputJq, readonly=true){
			StorageSearchSelect.input.getSearchButton(inputJq).prop("disabled", readonly);
			StorageSearchSelect.input.getNameInput(inputJq).prop("disabled", readonly);
			StorageSearchSelect.input.getClearButton(inputJq).prop("disabled", readonly);
		},
		setValue(inputJq, storageBlockId, storageBlockName = null){
			StorageSearchSelect.input.getIdInput(inputJq).val(storageBlockId);

			if(storageBlockName){
				StorageSearchSelect.input.getNameInput(inputJq).val(storageBlockName);
			} else {
				Getters.StorageBlock.getName(storageBlockId).then(function(name){
					StorageSearchSelect.input.getNameInput(inputJq).val(name);
				});
			}
		},
		setup(
			inputJq,
			storageBlockId = null,
			readOnly = false
		){
			StorageSearchSelect.input.clear(inputJq);

			if(storageBlockId){
				StorageSearchSelect.input.setValue(inputJq);
			}

			StorageSearchSelect.input.setReadonly(inputJq, readOnly);
		}
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


