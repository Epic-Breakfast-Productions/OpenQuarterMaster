ItemStoredSearchSelect = {
	modal: $("#itemStoredSearchSelectModal"),
	form: $("#itemStoredSearchSelectForm"),
	results: $("#itemStoredSearchSelectResults"),
	itemIdInput: $("#itemStoredSearchSelectForm-itemInputId"),
	itemSearchButton: $("#itemStoredSearchSelectForm-itemInputSearchButton"),
	itemNameInput: $("#itemStoredSearchSelectForm-itemInputName"),
	itemClearButton: $("#itemStoredSearchSelectForm-itemInputClearButton"),
	curDestinationId: null,

	getItemIdInput(storedItemInputGroupJq){
		return storedItemInputGroupJq.find("input[name=item]");
	},
	getBlockIdInput(storedItemInputGroupJq){
		return storedItemInputGroupJq.find("input[name=block]");
	},
	getIdInput(storedItemInputGroupJq){
		return storedItemInputGroupJq.find("input[name=itemStored]")
	},
	getLabelInput(storedItemInputGroupJq){
		return storedItemInputGroupJq.find("input[name=itemStoredLabel]");
	},
	enableInputs(storedItemInputGroupJq){
		storedItemInputGroupJq.find("button").attr("disabled", false);
	},
	disableInputs(storedItemInputGroupJq){
		storedItemInputGroupJq.find("button").attr("disabled", true);
	},
	selectStoredItem(storedLabel, storageBlock, storedItemId, inputGroupId, trigger = true) {
		console.log("Selected stored item: " + storedItemId + " - " + storedLabel);
		let inputGroup = $("#" + inputGroupId);
		let storedLabelJq = ItemStoredSearchSelect.getLabelInput(inputGroup);
		ItemStoredSearchSelect.getBlockIdInput(inputGroup).val(storageBlock);

		if (storedLabel == null) {
			Getters.StoredItem.getStored(
				"",
				storedItemId,
				function (storedData) {

					storedLabelJq.val(storedData.labelText);
				}
			);
		} else {
			storedLabelJq.val(storedLabel);
		}

		let storedIdInput = ItemStoredSearchSelect.getIdInput(inputGroup);
		storedIdInput.val(storedItemId);
		if (trigger) {
			storedIdInput.trigger("change");
		}
	},

	setupItemStoredSearchModal(buttonPressed) {
		console.log("setting up itemStoredSearchModal");
		ModalUtils.setReturnModal(ItemStoredSearchSelect.modal, buttonPressed);
		let inputGroup = $(buttonPressed).parent();
		let inputGroupId = inputGroup.attr("id");
		let itemId = ItemStoredSearchSelect.getItemIdInput(inputGroup).val();

		ItemStoredSearchSelect.itemIdInput.val(itemId);
		ItemStoredSearchSelect.itemNameInput.val("");
		Getters.InventoryItem.getItemName(itemId, function (itemName) {
			ItemStoredSearchSelect.itemNameInput.val(itemName);
		});

		ItemStoredSearchSelect.modal.attr("data-bs-destination", inputGroupId);
		ItemStoredSearchSelect.form.submit();
	},
	clearSearchInput(clearButtPushed, trigger = true) {
		let itemStoredInput = clearButtPushed.siblings("input[name=itemStored]");
		itemStoredInput.val("");
		clearButtPushed.siblings("input[name=itemStoredLabel]").val("");
		if (trigger) {
			itemStoredInput.trigger("change");
		}
	},
	resetSearchInput(itemStoredInputGroupJq) {
		ItemStoredSearchSelect.clearSearchInput(itemStoredInputGroupJq.find(".clearButton"), false);

		// clearButtPushed.siblings("input[name=itemName]").val("");
		ItemStoredSearchSelect.getItemIdInput(itemStoredInputGroupJq).val("");
		ItemStoredSearchSelect.getBlockIdInput(itemStoredInputGroupJq).val("");
		ItemStoredSearchSelect.enableInputs(itemStoredInputGroupJq);
	},
	/**
	 * Use this function to setup a stored input group for use
	 * @param storedItemInputGroupJq
	 * @param item
	 */
	setupInputs: async function(storedItemInputGroupJq, item, stored = null) {
		ItemStoredSearchSelect.resetSearchInput(storedItemInputGroupJq);
		if (typeof item === 'object' && item !== null && !Array.isArray(item)) {
			item = item.id;
		}
		storedItemInputGroupJq.find("input[name=item]").val(item);

		if(stored != null){
			let id = "";
			if (typeof stored === 'object' && !Array.isArray(stored)) {
				id = stored.id;
				ItemStoredSearchSelect.getBlockIdInput(storedItemInputGroupJq).val(stored.storageBlock);
				ItemStoredSearchSelect.getLabelInput(storedItemInputGroupJq).val(stored.labelText);
			} else {
				id = stored;
				Getters.StoredItem.getStored(item, stored, function(storedData) {
					ItemStoredSearchSelect.getBlockIdInput(storedItemInputGroupJq).val(storedData.storageBlock);
					ItemStoredSearchSelect.getLabelInput(storedItemInputGroupJq).val(storedData.labelText);
				});
			}
			ItemStoredSearchSelect.getIdInput(storedItemInputGroupJq).val(id);
			ItemStoredSearchSelect.disableInputs(storedItemInputGroupJq);
		}
	}
}

ItemStoredSearchSelect.form.on(
	"submit",
	function (event) {
		ItemStoredSearch.search(
			ItemStoredSearchSelect.form[0],
			event,
			ItemStoredSearchSelect.results,
			false,
			true,
			true,
			ItemStoredSearchSelect.modal.attr("data-bs-destination")
		);
	}
);

ItemStoredSearchSelect.itemNameInput.prop("readOnly", true);
ItemStoredSearchSelect.itemClearButton.prop("disabled", true);
ItemStoredSearchSelect.itemSearchButton.prop("disabled", true);