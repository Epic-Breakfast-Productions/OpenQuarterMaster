import {Rest} from "../../Rest.js";
import {ModalUtils} from "../../ModalUtils.js";
import {Getters} from "../Getters.js";
import {PageUtility} from "../../utilClasses/PageUtility.js";

export class StorageSearchSelect extends PageUtility {
	static storageSearchSelectModal = $("#storageSearchSelectModal");
	static storageSearchSelectForm = $("#storageSearchSelectForm");
	static storageSearchSelectResults = $("#storageSearchSelectResults");

	static setupSearch(returnWith, modalBackTo = null){
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
	}
	static selectStorageBlock(storageBlockId, storageBlockName = null){
		let returnWith = StorageSearchSelect.storageSearchSelectModal.data("search-select-return");
		console.log("Selected storage block: ",storageBlockId, storageBlockName, returnWith);

		if(typeof returnWith !== 'function'){//is input jq
			StorageSearchSelect.input.setValue(returnWith, storageBlockId, storageBlockName);
		} else {
			returnWith(storageBlockId, storageBlockName);
		}
	}


	static input = class {
		static clear(inputJq){
			inputJq = StorageSearchSelect.input.getInputJqFromInner(inputJq);

			StorageSearchSelect.input.getIdInput(inputJq).val("");
			StorageSearchSelect.input.getNameInput(inputJq).val("");
		}
		static getInputJqFromInner(innerElem){
			if(! (innerElem instanceof jQuery)){
				innerElem = $(innerElem);
			}
			return innerElem.closest(".storageBlockInput");
		}
		static getSearchButton(inputJq){
			return inputJq.find("button.searchButton");
		}
		static getIdInput(inputJq){
			return inputJq.find("input.idInput");
		}
		static getNameInput(inputJq){
			return inputJq.find("input.nameInput");
		}
		static getClearButton(inputJq){
			return inputJq.find("button.clearButton");
		}
		static setReadonly(inputJq, readonly=true){
			StorageSearchSelect.input.getSearchButton(inputJq).prop("disabled", readonly);
			StorageSearchSelect.input.getNameInput(inputJq).prop("disabled", readonly);
			StorageSearchSelect.input.getClearButton(inputJq).prop("disabled", readonly);
		}
		static setValue(inputJq, storageBlockId, storageBlockName = null){
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
		}
		static setup(
			inputJq,
			storageBlockId = null,
			readOnly = false
		){
			StorageSearchSelect.input.clear(inputJq);

			if(storageBlockId){
				StorageSearchSelect.input.setValue(inputJq, storageBlockId);
			}

			StorageSearchSelect.input.setReadonly(inputJq, readOnly);
		}
		static hasValue(inputJq){
			return StorageSearchSelect.input.getIdInput(inputJq).val() !== "";
		}
	}
	static {
		window.StorageSearchSelect = this;
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
}
