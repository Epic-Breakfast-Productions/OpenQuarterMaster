const ItemAddEdit = {
    addEditItemForm: $('#addEditItemForm'),
    addEditItemFormSubmitButton: $('#addEditItemFormSubmitButton'),
    addEditItemModal: $("#addEditItemModal"),
    addEditItemModalBs: new bootstrap.Modal("#addEditItemModal"),
    addEditItemFormMessages: $("#addEditItemFormMessages"),
    addEditItemModalLabel: $('#addEditItemModalLabel'),
    addEditItemModalLabelIcon: $('#addEditItemModalLabelIcon'),
    addEditItemFormMode: $('#addEditItemFormMode'),

    addEditItemIdInput: $("#addEditItemIdInput"),
    addEditItemNameInput: $('#addEditItemNameInput'),
    addEditItemDescriptionInput: $('#addEditItemDescriptionInput'),
    addEditItemBarcodeInput: $('#addEditItemBarcodeInput'),
    addEditItemPricePerUnitInput: $('#addEditItemPricePerUnitInput'),
    addEditItemExpiryWarningThresholdInput: $('#addEditItemExpiryWarningThresholdInput'),
    addEditItemExpiryWarningThresholdUnitInput: $('#addEditItemExpiryWarningThresholdUnitInput'),
    addEditItemCategoriesInput: $("#addEditItemCategoriesInput"),
    addEditItemTotalLowStockThresholdInput: $("#addEditItemTotalLowStockThresholdInput"),
    addEditItemTotalLowStockThresholdUnitInput: $("#addEditItemTotalLowStockThresholdUnitInput"),
    addEditItemStorageTypeInput: $('#addEditItemStorageTypeInput'),
    addEditItemUnitInput: $('#addEditItemUnitInput'),
    addEditItemIdentifyingAttInput: $('#addEditItemIdentifyingAttInput'),

    // itemNotStoredCheck: $("#addEditItemNotStoredCheck"),
    // itemNotStoredInputContainer: $("#addEditItemNotStoredInputContainer"),

    fileInput: $('#addEditItemForm').find(".fileAttachmentSelectInputTable"),
    addEditKeywordDiv: $('#addEditItemForm').find(".keywordInputDiv"),
    addEditAttDiv: $('#addEditItemForm').find(".attInputDiv"),
    addEditItemImagesSelected: $('#addEditItemForm').find(".imagesSelected"),
    associatedStorageInputContainer: $("#addEditItemAssociatedStorageInputContainer"),
    addEditItemTrackedItemIdentifierNameRow: $('#addEditItemTrackedItemIdentifierNameRow'),
    addEditItemUnitNameRow: $('#addEditItemUnitNameRow'),
    addEditItemPricePerUnitNameRow: $('#addEditItemPricePerUnitNameRow'),
    compatibleUnitOptions: "",


    numAmountStoredClicked: 0,
    numTrackedStoredClicked: 0,

    itemAdded(newItemName, newItemId) {
        PageMessages.reloadPageWithMessage("Added \"" + newItemName + "\" item successfully!", "success", "Success!");
    },

    async foreachStorageTypeFromInput(
        whenBulk,
        whenAmountList,
        whenUniqueMulti,
        whenUniqueSingle
    ) {
        await StorageTypeUtils.runForType(
            ItemAddEdit.addEditItemStorageTypeInput[0].value,
            whenBulk,
            whenAmountList,
            whenUniqueMulti,
            whenUniqueSingle
        );
    },
    async foreachStoredTypeFromStorageInput(
        whenAmount,
        whenUnique
    ) {
        await StorageTypeUtils.runForStoredType(
            ItemAddEdit.addEditItemStorageTypeInput[0].value,
            whenAmount,
            whenUnique
        );
    },

    resetAddEditForm() {
        ExtItemSearch.hideAddEditProductSearchPane();
        this.addEditItemIdInput.val("");
        this.addEditItemFormMode.val("");
        ItemAddEdit.addEditItemNameInput.val("");
        ItemAddEdit.addEditItemDescriptionInput.val("");
        ItemAddEdit.addEditItemBarcodeInput.val("");
        ItemAddEdit.addEditItemModalLabel.text("Item");
        // ItemAddEdit.addEditItemPricePerUnitInput.val("0.00");
        ItemAddEdit.addEditItemExpiryWarningThresholdInput.val(0);
        ItemAddEdit.addEditItemExpiryWarningThresholdUnitInput.prop('selectedIndex', 2);
        ItemAddEdit.addEditItemTotalLowStockThresholdInput.val("");
        ItemAddEdit.addEditItemIdentifyingAttInput.val("");
        ItemAddEdit.addEditItemStorageTypeInput.prop("disabled", false);
        ItemAddEdit.addEditItemStorageTypeInput.val($("#addEditItemStorageTypeInput option:first").val());
        Dselect.resetDselect(ItemAddEdit.addEditItemUnitInput);
        ItemAddEdit.addEditItemUnitInput.data("previous", ItemAddEdit.addEditItemUnitInput.val());
        Dselect.resetDselect(ItemAddEdit.addEditItemCategoriesInput);
        FileAttachmentSearchSelect.resetInput(this.fileInput);

        this.associatedStorageInputContainer.html("");

        // this.itemNotStoredCheck.attr("checked", false);
        // this.updateItemNotStored();
        // this.itemNotStoredInputContainer.text("");

        ItemAddEdit.addEditItemImagesSelected.text("");
        ItemAddEdit.addEditKeywordDiv.text("");
        ItemAddEdit.addEditAttDiv.text("");
    },
    setupAddEditForAdd() {
        console.log("Setting up add/edit form for add.");
        ItemAddEdit.resetAddEditForm();
        ItemAddEdit.addEditItemModalLabelIcon.html(Icons.iconWithSub(Icons.item, Icons.add));
        ItemAddEdit.addEditItemModalLabel.text("Item Add");
        ItemAddEdit.addEditItemFormMode.val("add");
        ItemAddEdit.addEditItemFormSubmitButton.html(Icons.iconWithSub(Icons.item, Icons.add) + " Add Item");
    },

    setupAddEditForEdit(itemId) {
        console.log("Setting up add/edit form for editing item " + itemId);
        ItemAddEdit.resetAddEditForm();
        ItemAddEdit.addEditItemModalLabel.text("Item Edit");
        ItemAddEdit.addEditItemFormMode.val("edit");
        ItemAddEdit.addEditItemModalLabelIcon.html(Icons.iconWithSub(Icons.item, Icons.edit));
        ItemAddEdit.addEditItemFormSubmitButton.html(Icons.iconWithSub(Icons.item, Icons.edit) + " Edit Item");

        ItemAddEdit.addEditItemStorageTypeInput.prop("disabled", true);

        Rest.call({
            spinnerContainer: ItemAddEdit.addEditItemModal,
            url: Rest.passRoot + "/inventory/item/" + itemId,
            failMessagesDiv: ItemAddEdit.addEditItemFormMessages,
            done: async function (data) {
                addSelectedImages(ItemAddEdit.addEditItemImagesSelected, data.imageIds);
                KeywordAttEdit.addKeywordInputs(ItemAddEdit.addEditKeywordDiv, data.keywords);
                KeywordAttEdit.addAttInputs(ItemAddEdit.addEditAttDiv, data.attributes);
                FileAttachmentSearchSelect.populateFileInputFromObject(
                    ItemAddEdit.fileInput,
                    data.attachedFiles,
                    ItemAddEdit.addEditItemModal,
                    ItemAddEdit.addEditItemFormMessages
                );

                ItemAddEdit.addEditItemIdInput.val(data.id);
                ItemAddEdit.addEditItemNameInput.val(data.name);
                ItemAddEdit.addEditItemDescriptionInput.val(data.description);
                ItemAddEdit.addEditItemStorageTypeInput.val(data.storageType);
                ItemAddEdit.addEditItemBarcodeInput.val(data.barcode);
                ItemAddEdit.addEditStoredTypeInputChanged();
                Dselect.setValues(ItemAddEdit.addEditItemCategoriesInput, data.categories);

                if (data.lowStockThreshold) {
                    console.log("Item had low stock threshold.");
                    ItemAddEdit.addEditItemTotalLowStockThresholdInput.val(data.lowStockThreshold.value)
                    ItemAddEdit.addEditItemTotalLowStockThresholdUnitInput.val(data.lowStockThreshold.unit.string)
                }


                if ((data.expiryWarningThreshold / 604800) % 1 == 0) {
                    console.log("Determined was weeks.");
                    ItemAddEdit.addEditItemExpiryWarningThresholdInput.val(data.expiryWarningThreshold / 604800);
                    ItemAddEdit.addEditItemExpiryWarningThresholdUnitInput.prop('selectedIndex', 4);
                } else if ((data.expiryWarningThreshold / 86400) % 1 == 0) {
                    console.log("Determined was days.");
                    ItemAddEdit.addEditItemExpiryWarningThresholdInput.val(data.expiryWarningThreshold / 86400);
                    ItemAddEdit.addEditItemExpiryWarningThresholdUnitInput.prop('selectedIndex', 3);
                } else if ((data.expiryWarningThreshold / 3600) % 1 == 0) {
                    console.log("Determined was hours.");
                    ItemAddEdit.addEditItemExpiryWarningThresholdInput.val(data.expiryWarningThreshold / 3600);
                    ItemAddEdit.addEditItemExpiryWarningThresholdUnitInput.prop('selectedIndex', 2);
                } else if ((data.expiryWarningThreshold / 60) % 1 == 0) {
                    console.log("Determined was minutes.");
                    ItemAddEdit.addEditItemExpiryWarningThresholdInput.val(data.expiryWarningThreshold / 60);
                    ItemAddEdit.addEditItemExpiryWarningThresholdUnitInput.prop('selectedIndex', 1);
                } else {
                    console.log("Determined was seconds.");
                    ItemAddEdit.addEditItemExpiryWarningThresholdInput.val(data.expiryWarningThreshold);
                    ItemAddEdit.addEditItemExpiryWarningThresholdUnitInput.prop('selectedIndex', 0);
                }

                data.storageBlocks.forEach(curStorageBlockId => {
                    getStorageBlockLabel(curStorageBlockId, function (label) {
                        //TODO:: determine if we are allowed to remove (if has stored items in it or not)
                        ItemAddEdit.storageInput.addStorage(label, curStorageBlockId);
                    });
                });
            }
        });
    },
    addEditStoredTypeInputChanged() {
        ItemAddEdit.foreachStoredTypeFromStorageInput(
            function () {
                ItemAddEdit.addEditItemUnitNameRow.show();
                ItemAddEdit.addEditItemUnitInput.prop('required', true);
                // ItemAddEdit.addEditItemPricePerUnitNameRow.show();
                // ItemAddEdit.addEditItemPricePerUnitInput.prop('required', true);
            },
            function () {
                ItemAddEdit.addEditItemUnitNameRow.hide();
                ItemAddEdit.addEditItemUnitInput.prop('required', false);
                // ItemAddEdit.addEditItemPricePerUnitNameRow.hide();
                // ItemAddEdit.addEditItemPricePerUnitInput.prop('required', false);

                // ItemAddEdit.addEditItemStorageTypeInput.attr('data-current', "TRACKED");
            }
        );
    },

    storageInput: {
        addStorage: function (blockName, blockId) {
            Main.processStart();
            let found = false;
            ItemAddEdit.associatedStorageInputContainer.find('input[name="storageBlocks[]"]').each(function () {
                if ($(this).val() === blockId) {
                    found = true;
                }
            });
            if (found) {
                console.log("Tried to add a block that was already present.");
                return;
            }

            let newBlock = $('<div class="col-3 blockSelection" data-block-id="">' +
                '  <input type="hidden" name="storageBlocks[]" />' +
                '  <div class="card">' +
                '    <div class="card-body">' +
                '      <p class="card-text blockInputName"></p>' +
                '    </div>' +
                '    <div class="card-footer text-body-secondary">' +
                '      <button class="btn btn-sm btn-outline-danger" type="button" onclick="ItemAddEdit.storageInput.removeStorage(this);">' + Icons.remove + '</button>' +
                '    </div>' +
                '  </div>' +
                '</div>');
            newBlock.attr("data-block-id", blockId);
            newBlock.find('input[name="storageBlocks[]"]').val(blockId);
            newBlock.find(".blockInputName").text(blockName);

            ItemAddEdit.associatedStorageInputContainer.append(newBlock);
            Main.processStop();
        },
        removeStorage: function (removeButtonClicked) {//or input card?
            if (confirm("Are you sure you want to\nremove this associated storage?")) {
                console.log("Removing associated storage.");
                removeButtonClicked.parentElement.parentElement.parentElement.remove();
            } else {
                console.log("User canceled removing the associated storage.");
            }
        },
        selectedStorageList: function () {
            return ItemAddEdit.associatedStorageInputContainer.find("input[name='storageBlocks[]']")
                .map(function () {
                    return $(this).val();
                }).get();
        }
    }
};

//prevent enter from submitting form on barcode; barcode scanners can add enter key automatically
ItemAddEdit.addEditItemBarcodeInput.on('keypress', function (e) {
    // Ignore enter keypress
    if (e.which === 13) {
        return false;
    }
});

StorageSearchSelect.selectStorageBlock = function (blockName, blockId, inputIdPrepend, otherModalId) {
    console.log("Selected " + blockId + " - " + blockName);
    ItemAddEdit.storageInput.addStorage(blockName, blockId);
}

ItemAddEdit.addEditItemForm.submit(async function (event) {
    event.preventDefault();
    console.log("Submitting add/edit form.");

    let addEditData = {
        name: ItemAddEdit.addEditItemNameInput.val(),
        description: ItemAddEdit.addEditItemDescriptionInput.val(),
        barcode: ItemAddEdit.addEditItemBarcodeInput.val(),
        storageType: ItemAddEdit.addEditItemStorageTypeInput.val(),
        expiryWarningThreshold: ItemAddEdit.addEditItemExpiryWarningThresholdInput.val() * ItemAddEdit.addEditItemExpiryWarningThresholdUnitInput.val(),
        lowStockThreshold: (ItemAddEdit.addEditItemTotalLowStockThresholdInput.val() ? UnitUtils.getQuantityObj(
            ItemAddEdit.addEditItemTotalLowStockThresholdInput.val(),
            ItemAddEdit.addEditItemTotalLowStockThresholdUnitInput.val()
        ) : null),
        categories: ItemCategoryInput.getValueFromInput(ItemAddEdit.addEditItemCategoriesInput),
        storageBlocks: ItemAddEdit.storageInput.selectedStorageList(),
        attachedFiles: FileAttachmentSearchSelect.getFileListFromInput(ItemAddEdit.fileInput)
    };

    let setAmountStoredVars = function () {
        addEditData["unit"] = {
            string: ItemAddEdit.addEditItemUnitInput.val()
        };
        addEditData["valuePerUnit"] = ItemAddEdit.addEditItemPricePerUnitInput.val();
    };

    ItemAddEdit.foreachStorageTypeFromInput(
        setAmountStoredVars,
        setAmountStoredVars,
        function () {

        }
    );

    KeywordAttEdit.addKeywordAttData(addEditData, ItemAddEdit.addEditKeywordDiv, ItemAddEdit.addEditAttDiv);
    addImagesToData(addEditData, ItemAddEdit.addEditItemImagesSelected);

    console.log("Data being submitted: " + JSON.stringify(addEditData));
    let verb = "";
    let result = false;
    if (ItemAddEdit.addEditItemFormMode.val() === "add") {
        verb = "Created";
        console.log("Adding new item.");
        await Rest.call({
            url: Rest.passRoot + "/inventory/item",
            method: "POST",
            data: addEditData,
            async: false,
            done: function (data) {
                console.log("Response from create request: " + JSON.stringify(data));
                result = true;
            },
            failMessagesDiv: ItemAddEdit.addEditItemFormMessages
        });
    } else if (ItemAddEdit.addEditItemFormMode.val() === "edit") {
        verb = "Edited";
        let id = ItemAddEdit.addEditItemIdInput.val();
        console.log("Editing storage block " + id);

        await Rest.call({
            url: Rest.passRoot + "/inventory/item/" + id,
            method: "PUT",
            data: addEditData,
            async: false,
            done: function (data) {
                console.log("Response from create request: " + JSON.stringify(data));
                result = true;
            },
            failMessagesDiv: ItemAddEdit.addEditItemFormMessages
        });
    }

    if (!result) {
        PageMessages.addMessageToDiv(ItemAddEdit.addEditItemFormMessages, "danger", "Failed to do " + verb + " item.", "Failed", null);
    } else {
        PageMessages.reloadPageWithMessage(verb + " item successfully!", "success", "Success!");
    }
});
