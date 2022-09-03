var itemAddSubtractTransferModal = $("#itemAddSubtractTransferModal");
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

function getSelectedStorageBlock(selectInput){
	return itemAddSubtractTransferFormItemData.storageMap[selectInput.val()];
}

function getSelectedFrom(){
	return getSelectedStorageBlock(itemAddSubtractTransferFromSelect);
}
function getSelectedTo(){
	return getSelectedStorageBlock(itemAddSubtractTransferFromSelect);
}

/**
 * Fills form details
 * @param data
 */
function fillAddSubTransFormControlsForAmountSimple() {
	console.log("Setting up addSubTransForm controls for AMOUNT_SIMPLE");


}

function fillAddSubTransFormControlsForAmountList() {
	console.log("Setting up addSubTransForm controls for AMOUNT_LIST");

}

function fillAddSubTransFormControlsForTracked() {
	console.log("Setting up addSubTransForm controls for AMOUNT_TRACKED");
}

function fillAddSubTransFormControls(){
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

	itemAddSubtractTransferFormItemImg.attr("src", "/api/media/image/for/item/" + itemId);

	doRestCall({
		url: "/api/inventory/item/" + itemId,
		done: function (data) {
			itemAddSubtractTransferFormItemData = data;
			itemAddSubtractTransferFormItemNameLabel.text(itemAddSubtractTransferFormItemData.name);
			itemAddSubtractTransferFormItemIdInput.val(itemAddSubtractTransferFormItemData.id);
			itemAddSubtractTransferFormItemTypeInput.val(itemAddSubtractTransferFormItemData.storageType);
			resetAddSubTransFormActionChanged();
		}
	});
}

