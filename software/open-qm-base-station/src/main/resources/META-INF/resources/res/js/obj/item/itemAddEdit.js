
//TODO:: add 'namespace'

var addEditItemForm = $('#addEditItemForm');
var addEditModal = $("#addEditModal");
var addEditFormId = $("#addEditFormId");
var addEditFormMessages = $("#addEditFormMessages");
var addEditModalLabel = $('#addEditModalLabel');
var addEditFormMode = $('#addEditFormMode');
var addEditKeywordDiv = addEditItemForm.find(".keywordInputDiv");
var addEditAttDiv = addEditItemForm.find(".attInputDiv");
var addEditNameInput = $('#addEditNameInput');
var addEditDescriptionInput = $('#addEditDescriptionInput');
var addEditBarcodeInput = $('#addEditBarcodeInput');
var addEditPricePerUnitInput = $('#addEditPricePerUnitInput');
var addEditExpiryWarningThresholdInput = $('#addEditExpiryWarningThresholdInput');
var addEditExpiryWarningThresholdUnitInput = $('#addEditExpiryWarningThresholdUnitInput');
var addEditTotalLowStockThresholdInput = $("#addEditTotalLowStockThresholdInput");
var addEditTotalLowStockThresholdUnitInput = $("#addEditTotalLowStockThresholdUnitInput");
var addEditStorageTypeInput = $('#addEditStorageTypeInput');
var addEditUnitInput = $('#addEditUnitInput');
var addEditIdentifyingAttInput = $('#addEditIdentifyingAttInput');
var addEditImagesSelected = addEditItemForm.find(".imagesSelected");

var storedContainer = $('#storedContainer');
var trackedItemIdentifierNameRow = $('#trackedItemIdentifierNameRow');
var unitNameRow = $('#unitNameRow');
var pricePerUnitNameRow = $('#pricePerUnitNameRow');
var compatibleUnitOptions = "";

//prevent enter from submitting form on barcode; barcode scanners can add enter key automatically
addEditBarcodeInput.on('keypress', function(e) {
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
		addEditStorageTypeInput[0].value,
		whenAmountSimple,
		whenAmountList,
		whenTracked
	);
}

function haveStored(){
	return storedContainer.children().length > 0;
}


updateCompatibleUnits(addEditUnitInput.val(), addEditItemForm);

function handleItemUnitChange(){
	if(haveStored() && !confirm("Doing this will reset all held units. Are you sure?")){
		//TODO:: handle changing back to old value
	} else {
		updateCompatibleUnits(addEditUnitInput.val(), addEditItemForm);
	}
}

function resetAddEditForm(){
	ExtItemSearch.hideAddEditProductSearchPane();
	addEditNameInput.val("");
	addEditDescriptionInput.val("");
	addEditBarcodeInput.val("");
	addEditModalLabel.text("Item");
	addEditPricePerUnitInput.val("0.00");
	addEditExpiryWarningThresholdInput.val(0);
	addEditExpiryWarningThresholdUnitInput.prop('selectedIndex',2);
	addEditTotalLowStockThresholdInput.val("");
	addEditIdentifyingAttInput.val("");
	addEditStorageTypeInput.prop( "disabled", false );
	addEditStorageTypeInput.val($("#addEditStorageTypeInput option:first").val());
	addEditUnitInput.val($("#addEditUnitInput option:first").val());

	setIdAttField();
	updateCompatibleUnits(addEditUnitInput.val(), storedContainer);

	addEditImagesSelected.text("");
	addEditKeywordDiv.text("");
	addEditAttDiv.text("");
}

function setupAddEditForAdd(){
	console.log("Setting up add/edit form for add.");
	resetAddEditForm();
	addEditModalLabel.text("Item Add");
	addEditFormMode.val("add");
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
	addEditModalLabel.text("Item Edit");
	addEditFormMode.val("edit");
	addEditStorageTypeInput.prop( "disabled", true );

	doRestCall({
		spinnerContainer: addEditModal,
		url: "/api/v1/inventory/item/" + itemId,
		failMessagesDiv: addEditFormMessages,
		done: async function(data){
			addSelectedImages(addEditImagesSelected, data.imageIds);
			addKeywordInputs(addEditKeywordDiv, data.keywords);
			addAttInputs(addEditAttDiv, data.attributes);

			addEditFormId.val(data.id);
			addEditNameInput.val(data.name);
			addEditDescriptionInput.val(data.description);
			addEditStorageTypeInput.val(data.storageType);
			addEditStoredTypeInputChanged();

			if(data.lowStockThreshold) {
				addEditTotalLowStockThresholdInput.val(data.lowStockThreshold.value)
				addEditTotalLowStockThresholdUnitInput.val(data.lowStockThreshold.unit.string)
			}

			let setAmountStoredVars = async function(){
				addEditUnitInput.val(data.unit.string);
				addEditPricePerUnitInput.val(data.valuePerUnit);
				await updateCompatibleUnits(addEditUnitInput.val(), storedContainer);
			};

			foreachStoredTypeFromAddEditInput(
				await setAmountStoredVars,
				await setAmountStoredVars,
				function(){
					addEditIdentifyingAttInput.val(data.trackedItemIdentifierName);
				}
			);

			if((data.expiryWarningThreshold / 604800) % 1 == 0){
				console.log("Determined was weeks.");
				addEditExpiryWarningThresholdInput.val(data.expiryWarningThreshold / 604800);
				addEditExpiryWarningThresholdUnitInput.prop('selectedIndex', 4);
			} else if((data.expiryWarningThreshold / 86400) % 1 == 0){
				console.log("Determined was days.");
				addEditExpiryWarningThresholdInput.val(data.expiryWarningThreshold / 86400);
				addEditExpiryWarningThresholdUnitInput.prop('selectedIndex', 3);
			} else if((data.expiryWarningThreshold / 3600) % 1 == 0){
				console.log("Determined was hours.");
				addEditExpiryWarningThresholdInput.val(data.expiryWarningThreshold / 3600);
				addEditExpiryWarningThresholdUnitInput.prop('selectedIndex', 2);
			} else if((data.expiryWarningThreshold / 60) % 1 == 0){
				console.log("Determined was minutes.");
				addEditExpiryWarningThresholdInput.val(data.expiryWarningThreshold / 60);
				addEditExpiryWarningThresholdUnitInput.prop('selectedIndex', 1);
			} else {
				console.log("Determined was seconds.");
				addEditExpiryWarningThresholdInput.val(data.expiryWarningThreshold);
				addEditExpiryWarningThresholdUnitInput.prop('selectedIndex', 0);
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
	storedContainer.html("");
	let value = addEditStorageTypeInput[0].value;

	if(addEditStorageTypeInput.attr('data-current') == null){
		addEditStorageTypeInput.attr('data-current', "AMOUNT_SIMPLE");
	} else {
		addEditStorageTypeInput.attr('data-current', value);
	}


	let whenAmount = function (){
		trackedItemIdentifierNameRow.hide();
		addEditIdentifyingAttInput.prop('required', false);
		unitNameRow.show();
		addEditUnitInput.prop('required',true);
		pricePerUnitNameRow.show();
		addEditPricePerUnitInput.prop('required',true);
	}

	foreachStoredTypeFromAddEditInput(
		whenAmount,
		whenAmount,
		function(){
			unitNameRow.hide();
			pricePerUnitNameRow.hide();
			addEditPricePerUnitInput.prop('required',false);
			addEditUnitInput.prop('required',false);
			trackedItemIdentifierNameRow.show();
			addEditIdentifyingAttInput.prop('required', true);
			addEditStorageTypeInput.attr('data-current', "TRACKED");
		}
	);
}

function addEditStoredTypeInputChanged(){
	if(haveStored() && !confirm("Changing the type of storage will clear all stored entries.\nAre you sure?")){
		addEditStorageTypeInput.val(
			addEditStorageTypeInput.attr('data-current')
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
		'     <select class="form-select amountStoredUnitInput unitInput" name="amountStoredUnit" onchange="addEditUpdateStoredHeader(\''+headerId+'\')">'+compatibleUnitOptions+'</select>\n'+ //TODO:: populate
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
		'        <div class="accordion-body storedContainer">\n'+
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
	storedContainer.find("[name=identifier]").each(function(i){
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
		'        <div class="accordion-body storedContainer">\n'+
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
	storedContainer.append(newBlockAccord);
}

function createStorageBlockAccord(blockName, blockId, add = true){
	let accordId = "addEditItemStorageAssoc-" + blockId;
	let existantAccord = storedContainer.find("#" + accordId);
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
			'        <div id="'+accordCollapseId+'" class="accordion-collapse collapse" aria-labelledby="'+accordHeaderId+'" data-bs-parent="#storedContainer">\n' +
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

function buildStoredObj(storedContainer, type){
	let output = {
		storedType: type
	};

	//TODO:: check this func for completeness of diff types
	if( type == "TRACKED"){
		output['identifier'] = storedContainer.find("[name=identifier]").val()
	}

	{
		let amountInput = storedContainer.find("[name=amountStored]");
		if (amountInput.length) {
			output["amount"] = getQuantityObj(
				parseFloat(amountInput.get(0).value),
				storedContainer.find("[name=amountStoredUnit]").get(0).value
			);
		}
	}

	{
		let input = storedContainer.find("[name=barcode]");
		if (input.length) {
			output["barcode"] = input.get(0).value;
		}
	}

	{
		let input = storedContainer.find("[name=condition]");
		if (input.length) {
			parseFloat(output["condition"] = input.get(0).value);
		}
	}
	{
		let input = storedContainer.find("[name=conditionDetails]");
		if (input.length) {
			output["conditionNotes"] = input.get(0).value;
		}
	}
	{
		let input = storedContainer.find("[name=expires]");
		if (input.length) {
			output["expires"] = input.get(0).value;
		}
	}
	addKeywordAttData(output, $(storedContainer.find(".keywordInputDiv").get(0)), $(storedContainer.find(".attInputDiv").get(0)));
	addImagesToData(output, $(storedContainer.find(".imagesSelected").get(0)));


	return output;
}

function removeItem(itemId){
	if(!confirm("Are you sure you want to delete this item? This cannot be undone.")){
		return;
	}
	console.log("Removing item " + itemId);

	doRestCall({
		url: "/api/v1/inventory/item/" + itemId,
		method: "DELETE",
		done: function(data) {
			console.log("Response from remove request: " + JSON.stringify(data));
			reloadPageWithMessage("Removed item successfully!", "success", "Success!");
		},
		fail: function(data) {
			console.warn("Bad response from remove attempt: " + JSON.stringify(data));
			addMessageToDiv(addEditFormMessages, "danger", "Failed to remove item.", "Failed", null);
		}
	});
}

addEditItemForm.submit(async function (event) {
	event.preventDefault();
	console.log("Submitting add/edit form.");

	let addEditData = {
		name: addEditNameInput.val(),
		description: addEditDescriptionInput.val(),
		barcode: addEditBarcodeInput.val(),
		storageType: addEditStorageTypeInput.val(),
		expiryWarningThreshold: addEditExpiryWarningThresholdInput.val() * addEditExpiryWarningThresholdUnitInput.val(),
		lowStockThreshold: (addEditTotalLowStockThresholdInput.val() ? getQuantityObj(
			addEditTotalLowStockThresholdInput.val(),
			addEditTotalLowStockThresholdUnitInput.val()
		) : null),
		storageMap: { }
	};

	let setAmountStoredVars = function () {
		addEditData["unit"] = {
			string: addEditUnitInput.val()
		};
		addEditData["valuePerUnit"] = addEditPricePerUnitInput.val();
	};

	foreachStoredTypeFromAddEditInput(
		setAmountStoredVars,
		setAmountStoredVars,
		function () {
			addEditData["trackedItemIdentifierName"] = addEditIdentifyingAttInput.val();
		}
	);

	addKeywordAttData(addEditData, addEditKeywordDiv, addEditAttDiv);
	addImagesToData(addEditData, addEditImagesSelected);

	storedContainer.find(".storageBlock").each(function (i, storageBlockElement) {
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
	if (addEditFormMode.val() === "add") {
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
			failMessagesDiv: addEditFormMessages
		});
	} else if (addEditFormMode.val() === "edit") {
		verb = "Edited";
		let id = addEditFormId.val();
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
			failMessagesDiv: addEditFormMessages
		});
	}

	if (!result) {
		addMessageToDiv(addEditFormMessages, "danger", "Failed to do "+verb+" item.", "Failed", null);
	} else {
		reloadPageWithMessage(verb + " item successfully!", "success", "Success!");
	}
});
