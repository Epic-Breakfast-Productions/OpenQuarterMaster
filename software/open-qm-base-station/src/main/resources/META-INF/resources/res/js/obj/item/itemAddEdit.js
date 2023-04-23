
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

	addEditKeywordDiv: $('#addEditItemForm').find(".keywordInputDiv"),
	addEditAttDiv: $('#addEditItemForm').find(".attInputDiv"),
	addEditItemImagesSelected: $('#addEditItemForm').find(".imagesSelected"),
	addEditItemStoredContainer: $('#addEditItemStoredContainer'),
	addEditItemTrackedItemIdentifierNameRow: $('#addEditItemTrackedItemIdentifierNameRow'),
	addEditItemUnitNameRow: $('#addEditItemUnitNameRow'),
	addEditItemPricePerUnitNameRow: $('#addEditItemPricePerUnitNameRow'),
	compatibleUnitOptions: "",

	itemAdded(newItemName, newItemId){
		PageMessages.reloadPageWithMessage("Added \""+newItemName+"\" item successfully!", "success", "Success!");
	}
}

//prevent enter from submitting form on barcode; barcode scanners can add enter key automatically
ItemAddEdit.addEditItemBarcodeInput.on('keypress', function(e) {
	// Ignore enter keypress
	if (e.which === 13) {
		return false;
	}
});


function foreachStoredTypeFromAddEditInput(
	whenAmountSimple,
	whenAmountList,
	whenTracked
){
	StoredTypeUtils.foreachStoredType(
		ItemAddEdit.addEditItemStorageTypeInput[0].value,
		whenAmountSimple,
		whenAmountList,
		whenTracked
	);
}

function haveStored(){
	return ItemAddEdit.addEditItemStoredContainer.children().length > 0;
}


updateCompatibleUnits(ItemAddEdit.addEditItemUnitInput.val(), ItemAddEdit.addEditItemForm);

function handleItemUnitChange(){
	if(haveStored() && !confirm("Doing this will reset all held units. Are you sure?")){
		//TODO:: handle changing back to old value;  [Bug]: On Item addEdit, canceling changing item unit fails to set back to old value #229
	} else {
		updateCompatibleUnits(ItemAddEdit.addEditItemUnitInput.val(), ItemAddEdit.addEditItemForm);
	}
}

function resetAddEditForm(){
	ExtItemSearch.hideAddEditProductSearchPane();
	ItemAddEdit.addEditItemNameInput.val("");
	ItemAddEdit.addEditItemDescriptionInput.val("");
	ItemAddEdit.addEditItemBarcodeInput.val("");
	ItemAddEdit.addEditItemModalLabel.text("Item");
	ItemAddEdit.addEditItemPricePerUnitInput.val("0.00");
	ItemAddEdit.addEditItemExpiryWarningThresholdInput.val(0);
	ItemAddEdit.addEditItemExpiryWarningThresholdUnitInput.prop('selectedIndex',2);
	ItemAddEdit.addEditItemTotalLowStockThresholdInput.val("");
	ItemAddEdit.addEditItemIdentifyingAttInput.val("");
	ItemAddEdit.addEditItemStorageTypeInput.prop( "disabled", false );
	ItemAddEdit.addEditItemStorageTypeInput.val($("#addEditItemStorageTypeInput option:first").val());
	ItemAddEdit.addEditItemUnitInput.val($("#addEditItemUnitInput option:first").val());
	//TODO:: merge check
	Dselect.resetDselect(ItemAddEdit.addEditItemCategoriesInput);
	Dselect.resetDselect(ItemAddEdit.addEditItemUnitInput);

	setIdAttField();
	updateCompatibleUnits(ItemAddEdit.addEditItemUnitInput.val(), ItemAddEdit.addEditItemStoredContainer);

	ItemAddEdit.addEditItemImagesSelected.text("");
	ItemAddEdit.addEditKeywordDiv.text("");
	ItemAddEdit.addEditAttDiv.text("");
}

function setupAddEditForAdd(){
	console.log("Setting up add/edit form for add.");
	resetAddEditForm();
	ItemAddEdit.addEditItemModalLabelIcon.html(Icons.add);
	ItemAddEdit.addEditItemModalLabel.text("Item Add");
	ItemAddEdit.addEditItemFormMode.val("add");
}

function setStoredItemVales(storedDivJq, storedData){
	let forAmount = function(){
		storedDivJq.find("[name=amountStored]")[0].value = storedData.amount.value;
		storedDivJq.find("[name=amountStoredUnit]")[0].value = storedData.amount.unit.string;
	};
	foreachStoredTypeFromAddEditInput(
		forAmount,
		forAmount,
		function(){
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
}

function setupAddEditForEdit(itemId){
	console.log("Setting up add/edit form for editing item " + itemId);
	resetAddEditForm();
	//TODO:: merge fix
	ItemAddEdit.addEditItemModalLabel.text("Item Edit");
	ItemAddEdit.addEditItemFormMode.val("edit");
	ItemAddEdit.addEditItemModalLabelIcon.html(Icons.edit);

	ItemAddEdit.addEditItemStorageTypeInput.prop( "disabled", true );

	doRestCall({
		spinnerContainer: ItemAddEdit.addEditItemModal,
		url: "/api/v1/inventory/item/" + itemId,
		failMessagesDiv: ItemAddEdit.addEditItemFormMessages,
		done: async function(data){
			addSelectedImages(ItemAddEdit.addEditItemImagesSelected, data.imageIds);
			addKeywordInputs(ItemAddEdit.addEditKeywordDiv, data.keywords);
			addAttInputs(ItemAddEdit.addEditAttDiv, data.attributes);

			ItemAddEdit.addEditItemIdInput.val(data.id);
			ItemAddEdit.addEditItemNameInput.val(data.name);
			ItemAddEdit.addEditItemDescriptionInput.val(data.description);
			ItemAddEdit.addEditItemStorageTypeInput.val(data.storageType);
			ItemAddEdit.addEditItemBarcodeInput.val(data.barcode);
			addEditStoredTypeInputChanged();
			Dselect.setValues(ItemAddEdit.addEditItemCategoriesInput, data.categories);

			if(data.lowStockThreshold) {
				ItemAddEdit.addEditItemTotalLowStockThresholdInput.val(data.lowStockThreshold.value)
				ItemAddEdit.addEditItemTotalLowStockThresholdUnitInput.val(data.lowStockThreshold.unit.string)
			}

			let setAmountStoredVars = async function(){
				ItemAddEdit.addEditItemUnitInput.val(data.unit.string);
				ItemAddEdit.addEditItemPricePerUnitInput.val(data.valuePerUnit);
				await updateCompatibleUnits(ItemAddEdit.addEditItemUnitInput.val(), ItemAddEdit.addEditItemStoredContainer);
			};

			foreachStoredTypeFromAddEditInput(
				await setAmountStoredVars,
				await setAmountStoredVars,
				function(){
					ItemAddEdit.addEditItemIdentifyingAttInput.val(data.trackedItemIdentifierName);
				}
			);

			if((data.expiryWarningThreshold / 604800) % 1 == 0){
				console.log("Determined was weeks.");
				ItemAddEdit.addEditItemExpiryWarningThresholdInput.val(data.expiryWarningThreshold / 604800);
				ItemAddEdit.addEditItemExpiryWarningThresholdUnitInput.prop('selectedIndex', 4);
			} else if((data.expiryWarningThreshold / 86400) % 1 == 0){
				console.log("Determined was days.");
				ItemAddEdit.addEditItemExpiryWarningThresholdInput.val(data.expiryWarningThreshold / 86400);
				ItemAddEdit.addEditItemExpiryWarningThresholdUnitInput.prop('selectedIndex', 3);
			} else if((data.expiryWarningThreshold / 3600) % 1 == 0){
				console.log("Determined was hours.");
				ItemAddEdit.addEditItemExpiryWarningThresholdInput.val(data.expiryWarningThreshold / 3600);
				ItemAddEdit.addEditItemExpiryWarningThresholdUnitInput.prop('selectedIndex', 2);
			} else if((data.expiryWarningThreshold / 60) % 1 == 0){
				console.log("Determined was minutes.");
				ItemAddEdit.addEditItemExpiryWarningThresholdInput.val(data.expiryWarningThreshold / 60);
				ItemAddEdit.addEditItemExpiryWarningThresholdUnitInput.prop('selectedIndex', 1);
			} else {
				console.log("Determined was seconds.");
				ItemAddEdit.addEditItemExpiryWarningThresholdInput.val(data.expiryWarningThreshold);
				ItemAddEdit.addEditItemExpiryWarningThresholdUnitInput.prop('selectedIndex', 0);
			}


			Object.keys(data.storageMap).forEach(curStorageBlockId => {
				let newStorageBody = createStorageBlockAccord("", curStorageBlockId);

				let storageBlockEntriesContainer = newStorageBody.find(".storageBlockEntriesContainer");

				foreachStoredTypeFromAddEditInput(
					function(){
						setStoredItemVales(newStorageBody, data.storageMap[curStorageBlockId].stored);
					},
					function (){
						data.storageMap[curStorageBlockId].stored.forEach(function(curStorageBlock){
							let curId = 'addEditItemStorageAssoc-'+curStorageBlockId+'-formContent';
							let newAmtStored = createNewAmountStored(curId, curId, false);

							setStoredItemVales(
								newAmtStored,
								curStorageBlock
							);
							storageBlockEntriesContainer.append(newAmtStored);
						});
					},
					function(){
						let addItemField = newStorageBody.find(".identifierValueInput")[0];
						let addItemButton = newStorageBody.find(".addTrackedItemButton");
						Object.keys(data.storageMap[curStorageBlockId].stored).forEach(curItemIdentifier => {
							addItemField.value = curItemIdentifier;
							let curId = 'addEditItemStorageAssoc-'+curStorageBlockId+'-formContent';
							let trackedStored = createNewTrackedStored(curId, addItemButton, false);

							setStoredItemVales(
								trackedStored,
								data.storageMap[curStorageBlockId].stored[curItemIdentifier]
							);
							storageBlockEntriesContainer.append(trackedStored);
						});
						addItemField.value = "";
					}
				);
				addStorageBlockAccord(newStorageBody);

				getStorageBlockLabel(curStorageBlockId, function (blockName){
					newStorageBody.find(".storageBlockName").text(blockName);
				});
			});
		}
	});

}

function setIdAttField(){
	ItemAddEdit.addEditItemStoredContainer.html("");
	let value = ItemAddEdit.addEditItemStorageTypeInput[0].value;

	if(ItemAddEdit.addEditItemStorageTypeInput.attr('data-current') == null){
		ItemAddEdit.addEditItemStorageTypeInput.attr('data-current', "AMOUNT_SIMPLE");
	} else {
		ItemAddEdit.addEditItemStorageTypeInput.attr('data-current', value);
	}


	let whenAmount = function (){
		ItemAddEdit.addEditItemTrackedItemIdentifierNameRow.hide();
		ItemAddEdit.addEditItemIdentifyingAttInput.prop('required', false);
		ItemAddEdit.addEditItemUnitNameRow.show();
		ItemAddEdit.addEditItemUnitInput.prop('required',true);
		ItemAddEdit.addEditItemPricePerUnitNameRow.show();
		ItemAddEdit.addEditItemPricePerUnitInput.prop('required',true);
	}

	foreachStoredTypeFromAddEditInput(
		whenAmount,
		whenAmount,
		function(){
			ItemAddEdit.addEditItemUnitNameRow.hide();
			ItemAddEdit.addEditItemPricePerUnitNameRow.hide();
			ItemAddEdit.addEditItemPricePerUnitInput.prop('required',false);
			ItemAddEdit.addEditItemUnitInput.prop('required',false);
			ItemAddEdit.addEditItemTrackedItemIdentifierNameRow.show();
			ItemAddEdit.addEditItemIdentifyingAttInput.prop('required', true);
			ItemAddEdit.addEditItemStorageTypeInput.attr('data-current', "TRACKED");
		}
	);
}

function addEditStoredTypeInputChanged(){
	if(haveStored() && !confirm("Changing the type of storage will clear all stored entries.\nAre you sure?")){
		ItemAddEdit.addEditItemStorageTypeInput.val(
			ItemAddEdit.addEditItemStorageTypeInput.attr('data-current')
		);
		return;
	}
	setIdAttField();
}

function removeStored(toRemoveId){
	if(!confirm("Are you sure? This can't be undone.")){
		return;
	}
	console.log("Removing.");
	$(toRemoveId).remove();
}

function getCommonStoredFormElements(headerId, toRemoveId) {
	return  '<div class="mb-3 ">\n'+
		'    <label class="form-label">Barcode</label>\n' +
		'    <div class="input-group">\n'+
		'        <input type="text" class="form-control storedBarcodeInput" name="barcode" placeholder="UPC, ISBN...">\n'+
		'    </div>\n'+
		'</div>\n' +
		'<div class="mb-3 ">\n'+
		'    <label class="form-label">Condition Percentage</label>\n' +
		'    <div class="input-group">\n'+
		'        <input type="number" max="100" min="0" step="any" class="form-control storedConditionPercentageInput" name="condition" ' + (headerId == null?'':'onchange="addEditUpdateStoredHeader(\''+headerId+'\')"')+'>\n'+ //TODO:: better label of better to worse
		'        <span class="input-group-text" id="addon-wrapping">%</span>\n'+ //TODO:: better label of better to worse
		'    </div>\n'+
		'</div>\n' +
		'<div class="mb-3">\n'+
		'    <label class="form-label">Condition Details</label>\n' +
		'    <textarea class="form-control" name="conditionDetails"></textarea>\n'+
		'</div>\n' +
		'<div class="mb-3">\n'+
		'    <label class="form-label">Expires</label>\n' +
		'    <input type="date" class="form-control storedExpiredInput" name="expires" ' + (headerId == null?'':'onchange="addEditUpdateStoredHeader(\''+headerId+'\')"')+'>\n'+ //TODO:: enforce future date?
		//TODO:: note to leave blank if not applicable
		'</div>\n' +
		imageInputTemplate.html() +
		keywordInputTemplate.html() +
		attInputTemplate.html() +
		'<div class="mb-3 ">\n'+
		'    <button type="button" class="btn btn-danger" onclick="removeStored(\'#'+toRemoveId+'\');">'+Icons.remove+' Remove Stored</button> '+
		'</div>\n'
		;
}

/**
 *
 * @param headerId
 * @param toRemoveId
 * @returns \{string}
 */
function getAmountStoredFormElements(headerId, toRemoveId) {
	return '<div class="input-group mt-2 mb-3">\n'+
		'     <input type="number" class="form-control amountStoredValueInput" name="amountStored" placeholder="Value" value="0.00" min="0.00" step="any" required onchange="addEditUpdateStoredHeader(\''+headerId+'\')">\n'+
		'     <select class="form-select amountStoredUnitInput unitInput" name="amountStoredUnit" onchange="addEditUpdateStoredHeader(\''+headerId+'\')">'+ItemAddEdit.compatibleUnitOptions+'</select>\n'+ //TODO:: populate
		'</div>\n'+
		getCommonStoredFormElements(headerId, toRemoveId);
}

/**
 *
 * @param headerId
 * @param toRemoveId
 * @returns \{string}
 */
function getTrackedStoredFormElements(headerId, toRemoveId) {
	return '<div class="mb-3">\n'+
		'    <label class="form-label">Identifier:</label>\n' +
		'    <input class="form-control" type="text" name="identifier" onchange="addEditUpdateStoredHeader(\''+headerId+'\')" required>\n'+ // TODO:: populate
		'</div>\n' +
		'<div class="mb-3">\n'+
		'    <label class="form-label">Identifying Details</label>\n' +
		'    <textarea class="form-control" name="identifyingDetails"></textarea>\n'+
		'</div>\n' +
		getCommonStoredFormElements(headerId, toRemoveId);
}

function addEditUpdateStoredHeader(containerOrHeaderId){
	let parentElem;
	let header;
	if(typeof containerOrHeaderId === 'string' || containerOrHeaderId instanceof String){
		header = $("#"+containerOrHeaderId);
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

	if(headerAmountDisplay.length){
		headerAmountDisplay.text(parentElem.find(".amountStoredValueInput").get(0).value);//.dataset.symbol);
	}
	if(headerUnitDisplay.length) {
		headerUnitDisplay.text(parentElem.find(".amountStoredUnitInput").get(0).value.replaceAll("\"", ""));
	}

	if(conditionDisplay.length) {
		let storedPercInput = parentElem.find(".storedConditionPercentageInput").get(0);
		if (storedPercInput.value) {
			header.find(".addEditConditionDisplayText").text(storedPercInput.value);
			conditionDisplay.show();
		} else {
			conditionDisplay.hide();
		}
	}

	if(addEditExpiresDisplay.length) {
		let storedExpInput = parentElem.find(".storedExpiredInput").get(0);
		if (storedExpInput.value) {
			header.find(".addEditExpiresDisplayText").text(storedExpInput.value);
			addEditExpiresDisplay.show();
		} else {
			addEditExpiresDisplay.hide();
		}
	}

	if(itemIdentifierDisplay.length){
		let itemIdInput = parentElem.find("[name=identifier]");
		if(itemIdInput.length) {
			itemIdentifierDisplay.text(itemIdInput.get(0).value);
		}
	}
}

function addNewAmountStored(amountStored, formContentId){
	$('#'+formContentId).append(amountStored);
}

var numAmountStoredClicked=0;
function createNewAmountStored(formContentId, parentId, add=true){
	var id="addEditAmountStoredEntry-"+(numAmountStoredClicked++);
	var headerId=id+"-header";
	var collapseId = id + "-collapse";

	var output = $(
		'<div class="accordion-item storedItem" id="'+id+'">\n'+
		'    <h2 class="accordion-header" id="'+headerId+'">\n'+
		'        <button class="accordion-button thinAccordion collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#'+collapseId+'" aria-expanded="false" aria-controls="'+collapseId+'">\n'+
		'          <span class="addEditAmountDisplay">0</span>\n' +
		'          <span class="addEditUnitDisplay"></span>&nbsp;&nbsp;\n'+
		'          <span class="addEditConditionDisplay">Condition: <span class="addEditConditionDisplayText"></span>%&nbsp;&nbsp;</span>\n'+
		'          <span class="addEditExpiresDisplay">Expires: <span class="addEditExpiresDisplayText"></span></span>\n'+ //TODO:: expires
		'        </button>\n'+
		'    </h2>\n'+
		'    <div id="'+collapseId+'" class="accordion-collapse collapse storage-list-entry" aria-labelledby="'+id+'" data-bs-parent="#'+parentId+'">\n'+
		'        <div class="accordion-body addEditItemStoredContainer">\n'+
		'            ' + getAmountStoredFormElements(headerId, id) +
		'        </div>\n'+
		'    </div>\n'+
		'</div>'
	);

	if(add){
		addNewAmountStored(output, formContentId);
	}
	addEditUpdateStoredHeader(id);
	updateStorageNumHeld(output);
	return output;
}

function createNewTrackedStored(trackedStored, formContentId){
	$('#'+formContentId).append(trackedStored);
}



var numTrackedStoredClicked=0;
function createNewTrackedStored(formContentId, caller, add = true){
	console.log("Adding new tracked storage item");

	let trackedStoredIdInput = $(caller).parent().find('.identifierValueInput').get(0);
	let trackedId = trackedStoredIdInput.value.trim();

	if(trackedId.length === 0){
		console.warn("No user input for id.");
		return;
	}
	let exists = false;
	ItemAddEdit.addEditItemStoredContainer.find("[name=identifier]").each(function(i){
		if(this.value.trim() === trackedId){
			exists = true;
		}
	});
	if(exists){
		console.warn("Id already exists.");
		alert("Identifier already exists");
		return;
	}

	trackedStoredIdInput.value = "";
	let id="addEditTrackedStoredEntry-"+(numTrackedStoredClicked++);
	let headerId=id+"-header";
	let collapseId = id + "-collapse";

	let output = $(
		'<div class="accordion-item storedItem" id="'+id+'">\n'+
		'    <h2 class="accordion-header" id="'+headerId+'">\n'+
		'        <button class="accordion-button thinAccordion collapsed itemIdentifierDisplay" type="button" data-bs-toggle="collapse" data-bs-target="#'+collapseId+'" aria-expanded="false" aria-controls="'+collapseId+'">\n'+
		'          ' + trackedId + '\n' +
		'        </button>\n'+
		'    </h2>\n'+
		'    <div id="'+collapseId+'" class="accordion-collapse collapse storage-list-entry" aria-labelledby="'+id+'" data-bs-parent="#'+formContentId+'">\n'+
		'        <div class="accordion-body addEditItemStoredContainer">\n'+
		'            ' + getTrackedStoredFormElements(headerId, id) +
		'        </div>\n'+
		'    </div>\n'+
		'</div>'
	);
	output.find("[name=identifier]").val(trackedId);

	addEditUpdateStoredHeader(id);
	updateStorageNumHeld(output);
	if(add) {
		$(caller).parent().parent().parent().find(".storageBlockEntriesContainer").append(output);
	}
	return output;
}

function updateStorageNumHeld(caller){
	//TODO:: search for parent with class (not working)
	// var parentAccord = $(caller).parent(".storedAccordion");
	//
	// parentAccord.find(".storageNumHeld").get(0).text(
	//         parentAccord.find(".storedItem").length
	// );
}

function addStorageBlockAccord(newBlockAccord){
	ItemAddEdit.addEditItemStoredContainer.append(newBlockAccord);
}

function createStorageBlockAccord(blockName, blockId, add = true){
	let accordId = "addEditItemStorageAssoc-" + blockId;
	let existantAccord = ItemAddEdit.addEditItemStoredContainer.find("#" + accordId);
	if(existantAccord.length){
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
		$('   <div class="accordion-item storedAccordion" id="'+accordId+'">\n'+
			'        <h2 class="accordion-header" id="'+accordHeaderId+'">\n'+
			'            <button class="accordion-button thinAccordion collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#'+accordCollapseId+'" aria-expanded="false" aria-controls="'+accordCollapseId+'">\n'+
			'                <img class="accordion-thumb" src="/api/v1/media/image/for/storageBlock/'+blockId+'" alt="'+blockName+' image">\n'+
			'                <span class="storageBlockName">'+blockName+'</span>\n'+
			'                &nbsp;(<span class="storageNumHeld">0</span>)\n'+
			'            </button>\n'+
			'        </h2>\n'+
			'        <div id="'+accordCollapseId+'" class="accordion-collapse collapse" aria-labelledby="'+accordHeaderId+'" data-bs-parent="#addEditItemStoredContainer">\n' +
			'            <div class="accordion-body" id="'+accordBodyId+'">\n'+
			'                <div id="'+accordFormContentId+'" class="accordion '+STORAGE_CLASS+' storageBlockEntriesContainer" data-storageBlockId="'+blockId+'"></div>\n'+
			'                <div id="'+accordButtonWrapperId+'" class="col d-grid gap-2"></div>\n'+
			'            </div>\n'+
			'        </div>\n'+
			'    </div>\n'
		);

	let newAccordBody = newStorage.find("#" + accordBodyId);
	let accordBodyButtonWrapper = newAccordBody.find("#"+accordButtonWrapperId);
	let accordBodyFormContentWrapper = newAccordBody.find("#"+accordFormContentId);

	foreachStoredTypeFromAddEditInput(
		function(){
			console.log("Setting up storage for AMOUNT_SIMPLE");

			accordBodyFormContentWrapper.append($(
				getAmountStoredFormElements(accordHeaderId, accordId)
			));
		},
		function(){
			console.log("Setting up storage for AMOUNT_LIST");

			accordBodyButtonWrapper.append($(
				'<button type="button" class="btn btn-sm btn-success mt-2 addAmountStoredButton" onclick="createNewAmountStored(\''+accordFormContentId+'\', \''+accordFormContentId+'\');">\n'+
				'    '+Icons.add+' Add\n'+
				'</button>\n'+
				'<button type="button" class="btn btn-sm btn-danger mt-2" onclick="if(confirm(\'Are you sure? This cannot be undone.\')){ $(\'#'+accordId+'\').remove();}">\n'+
				'    '+Icons.remove+' Remove Associated Storage\n'+
				'</button>'
			));
		},
		function(){
			console.log("Setting up storage for TRACKED");
			accordBodyButtonWrapper.append($(
				'<div class="input-group mt-2">\n'+
				'    <input type="text" class="form-control identifierValueInput" placeholder="Identifier Value">\n'+
				'    <button class="btn btn-outline-success addTrackedItemButton" type="button"  onclick="createNewTrackedStored(\''+accordFormContentId+'\', this);">' +
				'        '+Icons.add+' Add\n'+
				'    </button>\n'+
				'</div>'+
				'<button type="button" class="btn btn-sm btn-danger mt-2" onclick="if(confirm(\'Are you sure? This cannot be undone.\')){ $(\'#'+accordId+'\').remove();}">\n'+
				'    '+Icons.remove+' Remove Associated Storage\n'+
				'</button>'
			));
		}
	);

	if(add) {
		addStorageBlockAccord(newStorage);
	}
	return newStorage;
}

function selectStorageBlock(blockName, blockId, inputIdPrepend, otherModalId){
	console.log("Selected " + blockId + " - " + blockName);
	var newStorageBody = createStorageBlockAccord(blockName, blockId);
}

function buildStoredObj(addEditItemStoredContainer, type){
	let output = {
		storedType: type
	};

	//TODO:: check this func for completeness of diff types
	if( type == "TRACKED"){
		output['identifier'] = addEditItemStoredContainer.find("[name=identifier]").val()
	}

	{
		let amountInput = addEditItemStoredContainer.find("[name=amountStored]");
		if (amountInput.length) {
			output["amount"] = getQuantityObj(
				parseFloat(amountInput.get(0).value),
				addEditItemStoredContainer.find("[name=amountStoredUnit]").get(0).value
			);
		}
	}

	{
		let input = addEditItemStoredContainer.find("[name=barcode]");
		if (input.length) {
			output["barcode"] = input.get(0).value;
		}
	}

	{
		let input = addEditItemStoredContainer.find("[name=condition]");
		if (input.length) {
			parseFloat(output["condition"] = input.get(0).value);
		}
	}
	{
		let input = addEditItemStoredContainer.find("[name=conditionDetails]");
		if (input.length) {
			output["conditionNotes"] = input.get(0).value;
		}
	}
	{
		let input = addEditItemStoredContainer.find("[name=expires]");
		if (input.length) {
			output["expires"] = input.get(0).value;
		}
	}
	addKeywordAttData(output, $(addEditItemStoredContainer.find(".keywordInputDiv").get(0)), $(addEditItemStoredContainer.find(".attInputDiv").get(0)));
	addImagesToData(output, $(addEditItemStoredContainer.find(".imagesSelected").get(0)));


	return output;
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
		lowStockThreshold: (ItemAddEdit.addEditItemTotalLowStockThresholdInput.val() ? getQuantityObj(
			ItemAddEdit.addEditItemTotalLowStockThresholdInput.val(),
			ItemAddEdit.addEditItemTotalLowStockThresholdUnitInput.val()
		) : null),
		categories: ItemAddEdit.addEditItemCategoriesInput.val(),
		storageMap: { }
	};

	let setAmountStoredVars = function () {
		addEditData["unit"] = {
			string: ItemAddEdit.addEditItemUnitInput.val()
		};
		addEditData["valuePerUnit"] = ItemAddEdit.addEditItemPricePerUnitInput.val();
	};

	foreachStoredTypeFromAddEditInput(
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

		foreachStoredTypeFromAddEditInput(
			function () {
				storedVal = buildStoredObj(storageBlockElementJq, "AMOUNT");
			},
			function () {
				storedVal = [];
				storageBlockElementJq.find(".storage-list-entry").each(function (j, storedElement) {
					storedVal.push(buildStoredObj($(storedElement), "AMOUNT"));
				});
			},
			function () {
				storedVal = {};
				storageBlockElementJq.find(".storage-list-entry").each(function (j, storedElement) {
					let elementJq = $(storedElement);
					storedVal[elementJq.find("[name=identifier]").val()] = buildStoredObj(elementJq, "TRACKED");
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
		PageMessages.addMessageToDiv(ItemAddEdit.addEditItemFormMessages, "danger", "Failed to do "+verb+" item.", "Failed", null);
	} else {
		PageMessages.reloadPageWithMessage(verb + " item successfully!", "success", "Success!");
	}
});
