

var itemAddSubtractTransferModal = $("#itemAddSubtractTransferModal");
var itemAddSubtractTransferForm = $("#itemAddSubtractTransferForm");
var itemAddSubtractTransferFormItemImg = $("#itemAddSubtractTransferFormItemImg");
var itemAddSubtractTransferFormItemNameLabel = $("#itemAddSubtractTransferFormItemNameLabel");
var itemAddSubtractTransferFormItemIdInput = itemAddSubtractTransferForm.find("input[name=itemId]");
var itemAddSubtractTransferFormItemTypeInput = itemAddSubtractTransferForm.find("input[name=itemStorageType]");

function resetAddSubTransForm(){
	itemAddSubtractTransferFormItemNameLabel.text("");
	itemAddSubtractTransferFormItemIdInput.val("");
	itemAddSubtractTransferFormItemTypeInput.val("");
	itemAddSubtractTransferFormItemImg.attr("src", "");
}


function setupAddSubTransForm(itemId){
	console.log("Setting up addSubTrans form for item " + itemId);
	resetAddSubTransForm();

	itemAddSubtractTransferFormItemImg.attr("src", "/api/media/image/for/item/" + itemId);

	doRestCall({
		spinnerContainer: itemAddSubtractTransferModal,
		url: "/api/inventory/item/" + itemId,
		done: function (data) {
			itemAddSubtractTransferFormItemNameLabel.text(data.name);
			itemAddSubtractTransferFormItemIdInput.val(data.id);
			itemAddSubtractTransferFormItemTypeInput.val(data.storageType);
		}
	});
}