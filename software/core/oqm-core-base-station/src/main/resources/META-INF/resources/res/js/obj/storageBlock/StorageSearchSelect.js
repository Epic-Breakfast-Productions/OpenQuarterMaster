import {Rest} from "../../Rest.js";
import {ModalUtils} from "../../ModalUtils.js";
import {Getters} from "../Getters.js";

export const StorageSearchSelect = {
	storageSearchSelectModal: $("#storageSearchSelectModal"),
	storageSearchSelectForm: $("#storageSearchSelectForm"),
	storageSearchSelectResults: $("#storageSearchSelectResults"),

	setupSearch(returnWith, modalBackTo = null){
		console.log("Setting up storage block search select.", returnWith, modalBackTo);
		if(typeof returnWith !== 'function'){
			returnWith = StorageSearchSelect.input.getInputJqFromInner(returnWith);
			ModalUtils.setReturnModal(StorageSearchSelect.storageSearchSelectModal, returnWith);
		}

		if(modalBackTo){
			ModalUtils.setReturnModal(StorageSearchSelect.storageSearchSelectModal, modalBackTo);
		}

		console.debug("Returning with ", returnWith);

		StorageSearchSelect.storageSearchSelectForm.trigger("submit");
		StorageSearchSelect.storageSearchSelectModal.data("search-select-return", returnWith);
	},
	selectStorageBlock(storageBlockId, storageBlockName = null){
		let returnWith = StorageSearchSelect.storageSearchSelectModal.data("search-select-return");
		console.log("Selected storage block: ",storageBlockId, storageBlockName, returnWith);

		if(typeof returnWith !== 'function'){//is input jq
			StorageSearchSelect.input.setValue(returnWith, storageBlockId, storageBlockName);
		} else {
			returnWith(storageBlockId, storageBlockName);
		}
	},


	input: {
		clear(inputJq){
			inputJq = StorageSearchSelect.input.getInputJqFromInner(inputJq);

			StorageSearchSelect.input.getIdInput(inputJq).val("");
			StorageSearchSelect.input.getNameInput(inputJq).val("");
		},
		getInputJqFromInner(innerElem){
			if(! (innerElem instanceof jQuery)){
				innerElem = $(innerElem);
			}
			return innerElem.closest(".storageBlockInput");
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
			let idInput = StorageSearchSelect.input.getIdInput(inputJq);
			idInput.val(storageBlockId);

			if(storageBlockName){
				StorageSearchSelect.input.getNameInput(inputJq).val(storageBlockName);
			} else {
				Getters.StorageBlock.getStorageBlockLabel(storageBlockId, function(name){
					StorageSearchSelect.input.getNameInput(inputJq).val(name);
				});
			}
			idInput.trigger("change");
		},
		setup(
			inputJq,
			storageBlockId = null,
			readOnly = false
		){
			StorageSearchSelect.input.clear(inputJq);

			if(storageBlockId){
				StorageSearchSelect.input.setValue(inputJq, storageBlockId);
			}

			StorageSearchSelect.input.setReadonly(inputJq, readOnly);
		},
		hasValue(inputJq){
			return StorageSearchSelect.input.getIdInput(inputJq).val() !== "";
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


