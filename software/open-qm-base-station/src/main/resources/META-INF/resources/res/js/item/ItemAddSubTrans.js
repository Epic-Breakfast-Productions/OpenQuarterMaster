//TODO:: remove, move functionality to new
var itemAddSubtractTransferModal = $("#itemAddSubtractTransferModal");
var itemAddSubtractTransferFormMessages = $("#itemAddSubtractTransferFormMessages");
var itemAddSubtractTransferForm = $("#itemAddSubtractTransferForm");
var itemAddSubtractTransferFormItemImg = $("#itemAddSubtractTransferFormItemImg");
var itemAddSubtractTransferFormItemNameLabel = $("#itemAddSubtractTransferFormItemNameLabel");
var itemAddSubtractTransferFormItemIdInput = itemAddSubtractTransferForm.find("input[name=itemId]");
var itemAddSubtractTransferFormItemTypeInput = itemAddSubtractTransferForm.find("input[name=itemStorageType]");
var itemAddSubtractTransferOpSelect = $("#itemAddSubtractTransferOpSelect");
var itemAddSubtractTransferFromSelect = $("#itemAddSubtractTransferFromSelect");
var itemAddSubtractTransferFromSelectContainer = $("#itemAddSubtractTransferFromSelectContainer");
var itemAddSubtractTransferToSelect = $("#itemAddSubtractTransferToSelect");
var itemAddSubtractTransferToSelectContainer = $("#itemAddSubtractTransferToSelectContainer");
var itemAddSubtractTransferFromControls = $("#itemAddSubtractTransferFromControls");

var itemAddSubtractTransferFormItemData = undefined;

function resetAddSubTransForm() {
	itemAddSubtractTransferFormItemData = undefined;
	itemAddSubtractTransferFormMessages.text("");
	itemAddSubtractTransferFormItemNameLabel.text("");
	itemAddSubtractTransferFormItemIdInput.val("");
	itemAddSubtractTransferFormItemTypeInput.val("");
	itemAddSubtractTransferFormItemImg.attr("src", "");
	itemAddSubtractTransferOpSelect.val("add");
	itemAddSubtractTransferFromSelect.html("");
	itemAddSubtractTransferToSelect.html("");
	itemAddSubtractTransferFromSelectContainer.hide();
	itemAddSubtractTransferToSelectContainer.hide();
	itemAddSubtractTransferFromControls.val("");
}

function getSelectedStorageBlock(selectInput) {
	return itemAddSubtractTransferFormItemData.storageMap[selectInput.val()];
}

function getSelectedFrom() {
	return getSelectedStorageBlock(itemAddSubtractTransferFromSelect);
}

function getSelectedTo() {
	return getSelectedStorageBlock(itemAddSubtractTransferFromSelect);
}

function addAmountFormControls(container) {
	let formElements = $('<div class="row">' +
		'  <div class="col">' +
		'    <div class="input-group mt-2 mb-3">' +
		'      <input type="number" class="form-control amountStoredValueInput" name="amountStored" placeholder="Value" value="0" min="0" required="">' +
		'      <select class="form-select amountStoredUnitInput unitInput" name="amountStoredUnit">' +
		'      </select>' +
		'    </div>' +
		'  </div> ' +
		'</div>');


	if (container != undefined) {
		container.append(formElements);
	}
	return formElements;
}

/**
 * Fills form details
 * @param data
 */
function fillAddSubTransFormControlsForAmountSimple() {
	console.log("Setting up addSubTransForm controls for AMOUNT_SIMPLE");
	let controls = addAmountFormControls(itemAddSubtractTransferFromControls);
	updateCompatibleUnits(itemAddSubtractTransferFormItemData.unit.string, itemAddSubtractTransferFromControls);
}

function fillAddSubTransFormControlsForAmountList() {
	console.log("Setting up addSubTransForm controls for AMOUNT_LIST");
	//TODO
}

function fillAddSubTransFormControlsForTracked() {
	console.log("Setting up addSubTransForm controls for AMOUNT_TRACKED");
	//TODO
}

function fillAddSubTransFormControls() {
	let itemType = itemAddSubtractTransferFormItemTypeInput.val();
	switch (itemType) {
		case "AMOUNT_SIMPLE":
			fillAddSubTransFormControlsForAmountSimple();
			break;
		case "AMOUNT_LIST":
			fillAddSubTransFormControlsForAmountList();
			break;
		case "TRACKED":
			fillAddSubTransFormControlsForTracked();
			break;
	}
}

function setupAddSubTransFormFromToControls() {
	itemAddSubtractTransferFromControls.html("");
	let operationVal = itemAddSubtractTransferOpSelect.val();
	console.log("Setting up addSubTransForm for " + operationVal);
	switch (operationVal) {
		case "add":
			itemAddSubtractTransferFromSelectContainer.hide();
			itemAddSubtractTransferToSelectContainer.show();
			break;
		case "subtract":
			itemAddSubtractTransferFromSelectContainer.show();
			itemAddSubtractTransferToSelectContainer.hide();
			break;
		case "transfer":
			itemAddSubtractTransferToSelectContainer.show();
			itemAddSubtractTransferFromSelectContainer.show();
	}

	fillAddSubTransFormControls();
}

/**
 * Sets up form controls, including to/from based on
 * @param data
 */
function resetAddSubTransFormActionChanged() {
	Object.keys(itemAddSubtractTransferFormItemData.storageMap).forEach(curStorageId => {
		let curSelectFrom = $("<option></option>");
		let curSelectTo = $("<option></option>");
		curSelectFrom.val(curStorageId);
		curSelectTo.val(curStorageId);

		getStorageBlockLabel(
			curStorageId,
			function (label) {
				curSelectFrom.text(label);
				curSelectTo.text(label);
				itemAddSubtractTransferFromSelect.append(curSelectFrom);
				itemAddSubtractTransferToSelect.append(curSelectTo);
			}
		);

	});

	setupAddSubTransFormFromToControls();
}

function setupAddSubTransForm(itemId) {
	console.log("Setting up addSubTrans form for item " + itemId);
	resetAddSubTransForm();

	itemAddSubtractTransferFormItemImg.attr("src", "/api/v1/media/image/for/item/" + itemId);

	doRestCall({
		url: "/api/v1/inventory/item/" + itemId,
		done: function (data) {
			itemAddSubtractTransferFormItemData = data;
			itemAddSubtractTransferFormItemNameLabel.text(itemAddSubtractTransferFormItemData.name);
			itemAddSubtractTransferFormItemIdInput.val(itemAddSubtractTransferFormItemData.id);
			itemAddSubtractTransferFormItemTypeInput.val(itemAddSubtractTransferFormItemData.storageType);
			resetAddSubTransFormActionChanged();
		},
		failMessagesDiv: itemAddSubtractTransferFormMessages
	});
}

function getStoredDataFromItemAddSubtractTransferForm() {
	let data;
	switch (itemAddSubtractTransferFormItemTypeInput.val()) {
		case "AMOUNT_SIMPLE":
			data = {
				storedType: "AMOUNT",
				amount: getQuantityObj(
					itemAddSubtractTransferFromControls.find("input[name=amountStored]").val(),
					itemAddSubtractTransferFromControls.find("select[name=amountStoredUnit]").val()
				)
			}
			break;
		//TODO:: others
		default:
			console.error("Unimplemented storage type.");
			return;
	}

	return data;
}

itemAddSubtractTransferForm.on("submit", function (e) {
	e.preventDefault();
	console.log("Submitting operation request.");

	let data = getStoredDataFromItemAddSubtractTransferForm();
	let endpoint = "/api/v1/inventory/item/" + itemAddSubtractTransferFormItemIdInput.val() + "/";
	let method = undefined;

	switch (itemAddSubtractTransferOpSelect.val()) {
		case "add":
			endpoint += itemAddSubtractTransferToSelect.val();
			method = "PUT";
			break;
		case "subtract":
			endpoint += itemAddSubtractTransferFromSelect.val();
			method = "DELETE";
			break;
		case "transfer":
			endpoint += itemAddSubtractTransferFromSelect.val() + "/" + itemAddSubtractTransferToSelect.val();
			method = "PUT";
			break;
	}

	doRestCall({
		url: endpoint,
		data: data,
		method: method,
		done: function (data) {
			PageMessages.reloadPageWithMessage("Operation successful!", "success", "Success!");
		},
		failMessagesDiv: itemAddSubtractTransferFormMessages
	});


});