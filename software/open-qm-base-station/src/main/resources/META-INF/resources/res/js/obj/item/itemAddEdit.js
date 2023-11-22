//TODO:: finish adding to 'namespace'
const ItemAddEdit = {
	addEditItemForm: $('#addEditItemForm'),
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

	fileInput: $('#addEditItemForm').find(".fileAttachmentSelectInputTable"),
	addEditKeywordDiv: $('#addEditItemForm').find(".keywordInputDiv"),
	addEditAttDiv: $('#addEditItemForm').find(".attInputDiv"),
	addEditItemImagesSelected: $('#addEditItemForm').find(".imagesSelected"),
	addEditItemStoredContainer: $('#addEditItemStoredContainer'),
	addEditItemTrackedItemIdentifierNameRow: $('#addEditItemTrackedItemIdentifierNameRow'),
	addEditItemUnitNameRow: $('#addEditItemUnitNameRow'),
	addEditItemPricePerUnitNameRow: $('#addEditItemPricePerUnitNameRow'),
	compatibleUnitOptions: "",


	numAmountStoredClicked: 0,
	numTrackedStoredClicked: 0,

	itemAdded(newItemName, newItemId) {
		PageMessages.reloadPageWithMessage("Added \"" + newItemName + "\" item successfully!", "success", "Success!");
	},

	async foreachStoredTypeFromAddEditInput(
		whenAmountSimple,
		whenAmountList,
		whenTracked
	) {
		await StoredTypeUtils.foreachStoredType(
			ItemAddEdit.addEditItemStorageTypeInput[0].value,
			whenAmountSimple,
			whenAmountList,
			whenTracked
		);
	},
	haveStored() {
		return ItemAddEdit.addEditItemStoredContainer.children().length > 0;
	},
	handleItemUnitChange() {
		if (ItemAddEdit.haveStored() && !confirm("Doing this will reset all held units. Are you sure?")) {
			ItemAddEdit.addEditItemUnitInput.val(ItemAddEdit.addEditItemUnitInput.data("previous"));
			Dselect.resetDselect(ItemAddEdit.addEditItemCategoriesInput);
		} else {
			ItemAddEdit.addEditItemUnitInput.data("previous", ItemAddEdit.addEditItemUnitInput.val());
			UnitUtils.updateCompatibleUnits(ItemAddEdit.addEditItemUnitInput.val(), ItemAddEdit.addEditItemForm);
		}
	},
	resetAddEditForm() {
		ExtItemSearch.hideAddEditProductSearchPane();
		ItemAddEdit.addEditItemNameInput.val("");
		ItemAddEdit.addEditItemDescriptionInput.val("");
		ItemAddEdit.addEditItemBarcodeInput.val("");
		ItemAddEdit.addEditItemModalLabel.text("Item");
		ItemAddEdit.addEditItemPricePerUnitInput.val("0.00");
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

		ItemAddEdit.setIdAttField();
		UnitUtils.updateCompatibleUnits(ItemAddEdit.addEditItemUnitInput.val(), ItemAddEdit.addEditItemStoredContainer);

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
	},
	setStoredItemVales(storedDivJq, storedData) {
		let forAmount = function () {
			storedDivJq.find("[name=amountStored]")[0].value = storedData.amount.value;
			storedDivJq.find("[name=amountStoredUnit]")[0].value = storedData.amount.unit.string;
		};
		ItemAddEdit.foreachStoredTypeFromAddEditInput(
			forAmount,
			forAmount,
			function () {
				storedDivJq.find("[name=identifyingDetails]")[0].value = storedData.identifyingDetails;

			}
		);

		storedDivJq.find("[name=barcode]")[0].value = storedData.barcode;
		storedDivJq.find("[name=condition]")[0].value = storedData.condition;
		storedDivJq.find("[name=conditionDetails]")[0].value = storedData.conditionNotes;
		storedDivJq.find("[name=expires]")[0].value = storedData.expires;

		addSelectedImages(storedDivJq.find(".imagesSelected"), storedData.imageIds);
		addKeywordInputs(storedDivJq.find(".keywordInputDiv"), storedData.keywords);
		addAttInputs(storedDivJq.find(".attInputDiv"), storedData.attributes);
	},
	setupAddEditForEdit(itemId) {
		console.log("Setting up add/edit form for editing item " + itemId);
		ItemAddEdit.resetAddEditForm();
		ItemAddEdit.addEditItemModalLabel.text("Item Edit");
		ItemAddEdit.addEditItemFormMode.val("edit");
		ItemAddEdit.addEditItemModalLabelIcon.html(Icons.iconWithSub(Icons.item, Icons.edit));

		ItemAddEdit.addEditItemStorageTypeInput.prop("disabled", true);

		doRestCall({
			spinnerContainer: ItemAddEdit.addEditItemModal,
			url: "/api/v1/inventory/item/" + itemId,
			failMessagesDiv: ItemAddEdit.addEditItemFormMessages,
			done: async function (data) {
				addSelectedImages(ItemAddEdit.addEditItemImagesSelected, data.imageIds);
				addKeywordInputs(ItemAddEdit.addEditKeywordDiv, data.keywords);
				addAttInputs(ItemAddEdit.addEditAttDiv, data.attributes);

				ItemAddEdit.addEditItemIdInput.val(data.id);
				ItemAddEdit.addEditItemNameInput.val(data.name);
				ItemAddEdit.addEditItemDescriptionInput.val(data.description);
				ItemAddEdit.addEditItemStorageTypeInput.val(data.storageType);
				ItemAddEdit.addEditItemBarcodeInput.val(data.barcode);
				ItemAddEdit.addEditStoredTypeInputChanged();
				Dselect.setValues(ItemAddEdit.addEditItemCategoriesInput, data.categories);

				let setAmountStoredVars = function () {
					Dselect.setValues(ItemAddEdit.addEditItemUnitInput, data.unit.string);
					ItemAddEdit.addEditItemUnitInput.data("previous", ItemAddEdit.addEditItemUnitInput.val());
					ItemAddEdit.addEditItemPricePerUnitInput.val(data.valuePerUnit);
					UnitUtils.updateCompatibleUnits(ItemAddEdit.addEditItemUnitInput.val(), ItemAddEdit.addEditItemForm);
				};

				await ItemAddEdit.foreachStoredTypeFromAddEditInput(
					setAmountStoredVars,
					setAmountStoredVars,
					function () {
						ItemAddEdit.addEditItemIdentifyingAttInput.val(data.trackedItemIdentifierName);
					}
				);

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


				Object.keys(data.storageMap).forEach(curStorageBlockId => {
					let newStorageBody = ItemAddEdit.createStorageBlockAccord("", curStorageBlockId);

					let storageBlockEntriesContainer = newStorageBody.find(".storageBlockEntriesContainer");

					ItemAddEdit.foreachStoredTypeFromAddEditInput(
						function () {
							ItemAddEdit.setStoredItemVales(newStorageBody, data.storageMap[curStorageBlockId].stored);
						},
						function () {
							data.storageMap[curStorageBlockId].stored.forEach(function (curStorageBlock) {
								let curId = 'addEditItemStorageAssoc-' + curStorageBlockId + '-formContent';
								let newAmtStored = ItemAddEdit.createNewAmountStored(curId, curId, false);

								ItemAddEdit.setStoredItemVales(
									newAmtStored,
									curStorageBlock
								);
								storageBlockEntriesContainer.append(newAmtStored);
							});
						},
						function () {
							let addItemField = newStorageBody.find(".identifierValueInput")[0];
							let addItemButton = newStorageBody.find(".addTrackedItemButton");
							Object.keys(data.storageMap[curStorageBlockId].stored).forEach(curItemIdentifier => {
								addItemField.value = curItemIdentifier;
								let curId = 'addEditItemStorageAssoc-' + curStorageBlockId + '-formContent';
								let trackedStored = ItemAddEdit.createNewTrackedStored(curId, addItemButton, false);

								ItemAddEdit.setStoredItemVales(
									trackedStored,
									data.storageMap[curStorageBlockId].stored[curItemIdentifier]
								);
								storageBlockEntriesContainer.append(trackedStored);
							});
							addItemField.value = "";
						}
					);
					ItemAddEdit.addStorageBlockAccord(newStorageBody);

					getStorageBlockLabel(curStorageBlockId, function (blockName) {
						newStorageBody.find(".storageBlockName").text(blockName);
					});
				});
			}
		});
	},
	setIdAttField() {
		ItemAddEdit.addEditItemStoredContainer.html("");
		let value = ItemAddEdit.addEditItemStorageTypeInput[0].value;

		if (ItemAddEdit.addEditItemStorageTypeInput.attr('data-current') == null) {
			ItemAddEdit.addEditItemStorageTypeInput.attr('data-current', "AMOUNT_SIMPLE");
		} else {
			ItemAddEdit.addEditItemStorageTypeInput.attr('data-current', value);
		}


		let whenAmount = function () {
			ItemAddEdit.addEditItemTrackedItemIdentifierNameRow.hide();
			ItemAddEdit.addEditItemIdentifyingAttInput.prop('required', false);
			ItemAddEdit.addEditItemUnitNameRow.show();
			ItemAddEdit.addEditItemUnitInput.prop('required', true);
			ItemAddEdit.addEditItemPricePerUnitNameRow.show();
			ItemAddEdit.addEditItemPricePerUnitInput.prop('required', true);
		}

		ItemAddEdit.foreachStoredTypeFromAddEditInput(
			whenAmount,
			whenAmount,
			function () {
				ItemAddEdit.addEditItemUnitNameRow.hide();
				ItemAddEdit.addEditItemPricePerUnitNameRow.hide();
				ItemAddEdit.addEditItemPricePerUnitInput.prop('required', false);
				ItemAddEdit.addEditItemUnitInput.prop('required', false);
				ItemAddEdit.addEditItemTrackedItemIdentifierNameRow.show();
				ItemAddEdit.addEditItemIdentifyingAttInput.prop('required', true);
				ItemAddEdit.addEditItemStorageTypeInput.attr('data-current', "TRACKED");
			}
		);
	},
	removeStored(toRemoveId) {
		if (!confirm("Are you sure? This can't be undone.")) {
			return;
		}
		console.log("Removing.");
		$(toRemoveId).remove();
	},
	addEditStoredTypeInputChanged() {
		if (ItemAddEdit.haveStored() && !confirm("Changing the type of storage will clear all stored entries.\nAre you sure?")) {
			ItemAddEdit.addEditItemStorageTypeInput.val(
				ItemAddEdit.addEditItemStorageTypeInput.attr('data-current')
			);
			return;
		}
		ItemAddEdit.setIdAttField();
	},
	addEditUpdateStoredHeader(containerOrHeaderId) {
		let parentElem;
		let header;
		if (typeof containerOrHeaderId === 'string' || containerOrHeaderId instanceof String) {
			header = $("#" + containerOrHeaderId);
			parentElem = $(header.parent().get(0));
		} else {
			parentElem = containerOrHeaderId;
			header = parentElem.find(".accordion-header");
		}

		let headerAmountDisplay = header.find(".addEditAmountDisplay");
		let headerUnitDisplay = header.find(".addEditUnitDisplay");
		let conditionDisplay = header.find(".addEditConditionDisplay");
		let addEditExpiresDisplay = header.find(".addEditExpiresDisplay");

		let itemIdentifierDisplay = header.find(".itemIdentifierDisplay");

		if (headerAmountDisplay.length) {
			headerAmountDisplay.text(parentElem.find(".amountStoredValueInput").get(0).value);//.dataset.symbol);
		}
		if (headerUnitDisplay.length) {
			headerUnitDisplay.text(parentElem.find(".amountStoredUnitInput").get(0).value.replaceAll("\"", ""));
		}

		if (conditionDisplay.length) {
			let storedPercInput = parentElem.find(".storedConditionPercentageInput").get(0);
			if (storedPercInput.value) {
				header.find(".addEditConditionDisplayText").text(storedPercInput.value);
				conditionDisplay.show();
			} else {
				conditionDisplay.hide();
			}
		}

		if (addEditExpiresDisplay.length) {
			let storedExpInput = parentElem.find(".storedExpiredInput").get(0);
			if (storedExpInput.value) {
				header.find(".addEditExpiresDisplayText").text(storedExpInput.value);
				addEditExpiresDisplay.show();
			} else {
				addEditExpiresDisplay.hide();
			}
		}

		if (itemIdentifierDisplay.length) {
			let itemIdInput = parentElem.find("[name=identifier]");
			if (itemIdInput.length) {
				itemIdentifierDisplay.text(itemIdInput.get(0).value);
			}
		}
	},
	addNewAmountStored(amountStored, formContentId) {
		$('#' + formContentId).append(amountStored);
	},
	createNewAmountStored(formContentId, parentId, add = true) {
		var id = "addEditAmountStoredEntry-" + (ItemAddEdit.numAmountStoredClicked++);
		var headerId = id + "-header";
		var collapseId = id + "-collapse";

		var output = $(
			'<div class="accordion-item storedItem" id="' + id + '">\n' +
			'    <h2 class="accordion-header" id="' + headerId + '">\n' +
			'        <button class="accordion-button thinAccordion collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#' + collapseId + '" aria-expanded="false" aria-controls="' + collapseId + '">\n' +
			'          <span class="addEditAmountDisplay">0</span>\n' +
			'          <span class="addEditUnitDisplay"></span>&nbsp;&nbsp;\n' +
			'          <span class="addEditConditionDisplay">Condition: <span class="addEditConditionDisplayText"></span>%&nbsp;&nbsp;</span>\n' +
			'          <span class="addEditExpiresDisplay">Expires: <span class="addEditExpiresDisplayText"></span></span>\n' + //TODO:: expires
			'        </button>\n' +
			'    </h2>\n' +
			'    <div id="' + collapseId + '" class="accordion-collapse collapse storage-list-entry" aria-labelledby="' + id + '" data-bs-parent="#' + parentId + '">\n' +
			'        <div class="accordion-body addEditItemStoredContainer">\n' +
			'            ' + StoredEdit.getAmountStoredFormElements(headerId, id).prop('outerHTML') +
			'        </div>\n' +
			'    </div>\n' +
			'</div>'
		);

		if (add) {
			ItemAddEdit.addNewAmountStored(output, formContentId);
		}
		ItemAddEdit.addEditUpdateStoredHeader(id);
		ItemAddEdit.updateStorageNumHeld(output);
		return output;
	},
	createNewTrackedStored(formContentId, caller, add = true) {
		console.log("Adding new tracked storage item");

		let trackedStoredIdInput = $(caller).parent().find('.identifierValueInput').get(0);
		let trackedId = trackedStoredIdInput.value.trim();

		if (trackedId.length === 0) {
			console.warn("No user input for id.");
			return;
		}
		let exists = false;
		ItemAddEdit.addEditItemStoredContainer.find("[name=identifier]").each(function (i) {
			if (this.value.trim() === trackedId) {
				exists = true;
			}
		});
		if (exists) {
			console.warn("Id already exists.");
			alert("Identifier already exists");
			return;
		}

		trackedStoredIdInput.value = "";
		let id = "addEditTrackedStoredEntry-" + (ItemAddEdit.numTrackedStoredClicked++);
		let headerId = id + "-header";
		let collapseId = id + "-collapse";

		//TODO:: this properly
		let output = $(
			'<div class="accordion-item storedItem" id="' + id + '">\n' +
			'    <h2 class="accordion-header" id="' + headerId + '">\n' +
			'        <button class="accordion-button thinAccordion collapsed itemIdentifierDisplay" type="button" data-bs-toggle="collapse" data-bs-target="#' + collapseId + '" aria-expanded="false" aria-controls="' + collapseId + '">\n' +
			'          ' + trackedId + '\n' +
			'        </button>\n' +
			'    </h2>\n' +
			'    <div id="' + collapseId + '" class="accordion-collapse collapse storage-list-entry" aria-labelledby="' + id + '" data-bs-parent="#' + formContentId + '">\n' +
			'        <div class="accordion-body addEditItemStoredContainer">\n' +
			'            ' + StoredEdit.getTrackedStoredFormElements(headerId, id).prop('outerHTML') +
			'        </div>\n' +
			'    </div>\n' +
			'</div>'
		);
		output.find("[name=identifier]").val(trackedId);

		ItemAddEdit.addEditUpdateStoredHeader(id);
		ItemAddEdit.updateStorageNumHeld(output);
		if (add) {
			$(caller).parent().parent().parent().find(".storageBlockEntriesContainer").append(output);
		}
		return output;
	},
	updateStorageNumHeld(caller) {
		//TODO:: search for parent with class (not working)
		// var parentAccord = $(caller).parent(".storedAccordion");
		//
		// parentAccord.find(".storageNumHeld").get(0).text(
		//         parentAccord.find(".storedItem").length
		// );
	},
	addStorageBlockAccord(newBlockAccord) {
		ItemAddEdit.addEditItemStoredContainer.append(newBlockAccord);
	},
	createStorageBlockAccord(blockName, blockId, add = true) {
		let accordId = "addEditItemStorageAssoc-" + blockId;
		let existantAccord = ItemAddEdit.addEditItemStoredContainer.find("#" + accordId);
		if (existantAccord.length) {
			console.log("Already had association with storage block " + blockId);
			//TODO:: open block section instead of alerting
			alert("Storage block already present.");
			return null;
		}
		let accordHeaderId = accordId + "-header";
		let accordCollapseId = accordId + "-collapse";
		let accordBodyId = accordId + "-body";
		let accordFormContentId = accordId + "-formContent";
		let accordButtonWrapperId = accordId + "-formAddButtonWraper";

		let newStorage =
			$('   <div class="accordion-item storedAccordion" id="' + accordId + '">\n' +
				'        <h2 class="accordion-header" id="' + accordHeaderId + '">\n' +
				'            <button class="accordion-button thinAccordion collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#' + accordCollapseId + '" aria-expanded="false" aria-controls="' + accordCollapseId + '">\n' +
				'                <img class="accordion-thumb" src="/api/v1/media/image/for/storageBlock/' + blockId + '" alt="' + blockName + ' image">\n' +
				'                <span class="storageBlockName">' + blockName + '</span>\n' +
				// TODO::: this'                &nbsp;(<span class="storageNumHeld">0</span>)\n'+
				'            </button>\n' +
				'        </h2>\n' +
				'        <div id="' + accordCollapseId + '" class="accordion-collapse collapse" aria-labelledby="' + accordHeaderId + '" data-bs-parent="#addEditItemStoredContainer">\n' +
				'            <div class="accordion-body" id="' + accordBodyId + '">\n' +
				'                <div id="' + accordFormContentId + '" class="accordion ' + STORAGE_CLASS + ' storageBlockEntriesContainer" data-storageBlockId="' + blockId + '"></div>\n' +
				'                <div id="' + accordButtonWrapperId + '" class="col d-grid gap-2"></div>\n' +
				'            </div>\n' +
				'        </div>\n' +
				'    </div>\n'
			);

		let newAccordBody = newStorage.find("#" + accordBodyId);
		let accordBodyButtonWrapper = newAccordBody.find("#" + accordButtonWrapperId);
		let accordBodyFormContentWrapper = newAccordBody.find("#" + accordFormContentId);

		ItemAddEdit.foreachStoredTypeFromAddEditInput(
			function () {
				console.log("Setting up storage for AMOUNT_SIMPLE");

				accordBodyFormContentWrapper.append(StoredEdit.getAmountStoredFormElements(accordHeaderId, accordId));
			},
			function () {
				console.log("Setting up storage for AMOUNT_LIST");

				accordBodyButtonWrapper.append($(
					'<button type="button" class="btn btn-sm btn-success mt-2 addAmountStoredButton" onclick="ItemAddEdit.createNewAmountStored(\'' + accordFormContentId + '\', \'' + accordFormContentId + '\');">\n' +
					'    ' + Icons.add + ' Add\n' +
					'</button>\n' +
					'<button type="button" class="btn btn-sm btn-danger mt-2" onclick="if(confirm(\'Are you sure? This cannot be undone.\')){ $(\'#' + accordId + '\').remove();}">\n' +
					'    ' + Icons.remove + ' Remove Associated Storage\n' +
					'</button>'
				));
			},
			function () {
				console.log("Setting up storage for TRACKED");
				accordBodyButtonWrapper.append($(
					'<div class="input-group mt-2">\n' +
					'    <input type="text" class="form-control identifierValueInput" placeholder="Identifier Value">\n' +
					'    <button class="btn btn-outline-success addTrackedItemButton" type="button"  onclick="ItemAddEdit.createNewTrackedStored(\'' + accordFormContentId + '\', this);">' +
					'        ' + Icons.add + ' Add\n' +
					'    </button>\n' +
					'</div>' +
					'<button type="button" class="btn btn-sm btn-danger mt-2" onclick="if(confirm(\'Are you sure? This cannot be undone.\')){ $(\'#' + accordId + '\').remove();}">\n' +
					'    ' + Icons.remove + ' Remove Associated Storage\n' +
					'</button>'
				));
			}
		);

		if (add) {
			ItemAddEdit.addStorageBlockAccord(newStorage);
		}
		return newStorage;
	}
}

//prevent enter from submitting form on barcode; barcode scanners can add enter key automatically
ItemAddEdit.addEditItemBarcodeInput.on('keypress', function (e) {
	// Ignore enter keypress
	if (e.which === 13) {
		return false;
	}
});
UnitUtils.updateCompatibleUnits(ItemAddEdit.addEditItemUnitInput.val(), ItemAddEdit.addEditItemForm);

StorageSearchSelect.selectStorageBlock = function (blockName, blockId, inputIdPrepend, otherModalId) {
	console.log("Selected " + blockId + " - " + blockName);
	var newStorageBody = ItemAddEdit.createStorageBlockAccord(blockName, blockId);
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
		categories: ItemAddEdit.addEditItemCategoriesInput.val(),
		storageMap: {}
	};

	let setAmountStoredVars = function () {
		addEditData["unit"] = {
			string: ItemAddEdit.addEditItemUnitInput.val()
		};
		addEditData["valuePerUnit"] = ItemAddEdit.addEditItemPricePerUnitInput.val();
	};

	ItemAddEdit.foreachStoredTypeFromAddEditInput(
		setAmountStoredVars,
		setAmountStoredVars,
		function () {
			addEditData["trackedItemIdentifierName"] = ItemAddEdit.addEditItemIdentifyingAttInput.val();
		}
	);

	addKeywordAttData(addEditData, ItemAddEdit.addEditKeywordDiv, ItemAddEdit.addEditAttDiv);
	addImagesToData(addEditData, ItemAddEdit.addEditItemImagesSelected);

	ItemAddEdit.addEditItemStoredContainer.find(".storageBlock").each(function (i, storageBlockElement) {
		let storageBlockElementJq = $(storageBlockElement);
		let curStorageId = storageBlockElementJq.attr('data-storageBlockId');
		let storedVal;

		ItemAddEdit.foreachStoredTypeFromAddEditInput(
			function () {
				storedVal = StoredEdit.buildStoredObj(storageBlockElementJq, "AMOUNT");
			},
			function () {
				storedVal = [];
				storageBlockElementJq.find(".storage-list-entry").each(function (j, storedElement) {
					storedVal.push(StoredEdit.buildStoredObj($(storedElement), "AMOUNT"));
				});
			},
			function () {
				storedVal = {};
				storageBlockElementJq.find(".storage-list-entry").each(function (j, storedElement) {
					let elementJq = $(storedElement);
					storedVal[elementJq.find("[name=identifier]").val()] = StoredEdit.buildStoredObj(elementJq, "TRACKED");
				});
			}
		);

		storedVal = {
			stored: storedVal
		};

		addEditData.storageMap[curStorageId] = storedVal;
	});

	console.log("Data being submitted: " + JSON.stringify(addEditData));
	let verb = "";
	let result = false;
	if (ItemAddEdit.addEditItemFormMode.val() === "add") {
		verb = "Created";
		console.log("Adding new item.");
		await doRestCall({
			url: "/api/v1/inventory/item",
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

		await doRestCall({
			url: "/api/v1/inventory/item/" + id,
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
