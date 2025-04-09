ItemStoredSearchSelect = {
    modal: $("#itemStoredSearchSelectModal"),
    form: $("#itemStoredSearchSelectForm"),
    results: $("#itemStoredSearchSelectResults"),
    itemIdInput: $("#itemStoredSearchSelectForm-itemInputId"),
    itemSearchButton: $("#itemStoredSearchSelectForm-itemInputSearchButton"),
    itemNameInput: $("#itemStoredSearchSelectForm-itemInputName"),
    itemClearButton: $("#itemStoredSearchSelectForm-itemInputClearButton"),
    curDestinationId: null,

    selectStoredItem(storedLabel, storedItemId, inputGroupId) {
        console.log("Selected stored item: " + storedItemId + " - " + storedLabel);
        let inputGroup = $("#" + inputGroupId);

        inputGroup.find("input[name=itemStoredLabel]").val(storedLabel);
        let storedIdInput = inputGroup.find("input[name=itemStored]");
        storedIdInput.val(storedItemId);
        storedIdInput.trigger("change");
    },

    setupItemStoredSearchModal(buttonPressed) {
        console.log("setting up itemStoredSearchModal");
        ModalHelpers.setReturnModal(ItemStoredSearchSelect.modal, buttonPressed);
        let inputGroup = $(buttonPressed).parent();
        let inputGroupId = inputGroup.attr("id");
        let itemId = inputGroup.find("input[name=item]").val();

        ItemStoredSearchSelect.itemIdInput.val(itemId);
        ItemStoredSearchSelect.itemNameInput.val("");
        Getters.InventoryItem.getItemName(itemId, function(itemName){
            ItemStoredSearchSelect.itemNameInput.val(itemName);
        });

        ItemStoredSearchSelect.modal.attr("data-bs-destination", inputGroupId);
        ItemStoredSearchSelect.form.submit();
    },
    clearSearchInput(clearButtPushed, trigger = true){
        let itemStoredInput = clearButtPushed.siblings("input[name=itemStored]")
        itemStoredInput.val("");
        if(trigger) {
            itemStoredInput.trigger("change");
        }
    },
    resetSearchInput(itemStoredInputGroupJq){
        ItemStoredSearchSelect.clearSearchInput(itemStoredInputGroupJq.find(".clearButton"), false);

        // clearButtPushed.siblings("input[name=itemName]").val("");
        itemStoredInputGroupJq.find("input[name=item]").val("");
    },
    /**
     * Use this function to setup a stored input group for use
     * @param storedItemInputGroupJq
     * @param item
     */
    setupInputs(storedItemInputGroupJq, item){
        ItemStoredSearchSelect.resetSearchInput(storedItemInputGroupJq);
        if(typeof item === 'object' && item !== null && !Array.isArray(item)){
            item = item.id;
        }
        storedItemInputGroupJq.find("input[name=item]").val(item);
    },
}

ItemStoredSearchSelect.form.on(
    "submit",
    function(event){
        ItemStoredSearch.search(
            ItemStoredSearchSelect.form[0],
            event,
            ItemStoredSearchSelect.results,
            false,
            true,
            true,
            ItemStoredSearchSelect.modal.attr("data-bs-destination") //TODO:: need to figure out best way to dismiss to get back to desired modal
        );
    }
);

ItemStoredSearchSelect.itemNameInput.prop("readOnly", true);
ItemStoredSearchSelect.itemClearButton.prop("disabled", true);
ItemStoredSearchSelect.itemSearchButton.prop("disabled", true);