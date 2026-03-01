import {Rest} from "../../Rest.js";
import {ModalUtils} from "../../ModalUtils.js";

export const ItemSearchSelect = {
	itemSearchSelectModal: $("#itemSearchSelectModal"),
	itemSearchSelectForm: $("#itemSearchSelectForm"),
	itemSearchSelectResults: $("#itemSearchSelectResults"),

	selectItem(itemName, itemId, inputIdPrepend, otherModalId) {
		console.log("Selected item: " + itemId + " - " + itemName);
		let nameInputId = inputIdPrepend + "Id";
		let nameInputName = inputIdPrepend + "Name";

		$("#" + nameInputId).val(itemId);
		$("#" + nameInputName).val(itemName);
		$("#" + nameInputId).trigger("change");
	},
	setupItemSearchModal(inputIdPrepend, buttonPressed) {
		console.log("setting up itemSearchModal:", inputIdPrepend);
		ModalUtils.setReturnModal(ItemSearchSelect.itemSearchSelectModal, buttonPressed);
		ItemSearchSelect.itemSearchSelectModal.attr("data-bs-inputIdPrepend", inputIdPrepend);
		ItemSearchSelect.itemSearchSelectForm.submit();
	},
	clearSearchInput(clearButtPushed, trigger = true){
		//TODO:: update to be okay with container or button pushed
		clearButtPushed.siblings("input[name=itemName]").val("");
		clearButtPushed.siblings("input[name=item]").val("");
		if(trigger) {
			clearButtPushed.siblings("input[name=item]").trigger("change");
		}
	},

	initPage: function () {
		console.log("Initializing item search select.");
		ItemSearchSelect.itemSearchSelectForm.on("submit", function (event) {
			event.preventDefault();
			console.log("Submitting search form.");

			var searchParams = new URLSearchParams(new FormData(event.target));
			console.log("URL search params: " + searchParams);

			Rest.call({
				spinnerContainer: ItemSearchSelect.itemSearchSelectModal.get(0),
				url: Rest.passRoot + "/inventory/item?" + searchParams,
				returnType:"html",
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
		console.log("Done initializing item search select.");
	}
};
