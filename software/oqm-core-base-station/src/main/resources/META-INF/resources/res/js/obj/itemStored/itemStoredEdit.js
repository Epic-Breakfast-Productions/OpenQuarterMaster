
const ItemStoredEdit = {
	modal: $("#itemStoredEditModal"),
	form: $("#itemStoredEditForm"),
	formMessages: $("#itemStoredEditMessages"),
	resetForm(){
		ItemStoredEdit.form.attr("action", "");
		ItemStoredEdit.form.html("");
	},
	setupEditForm: async function(buttonPressed, itemId, stored){
		Main.processStart();
		if (typeof stored === "string" || (stored instanceof String)) {
			return Getters.StoredItem.getStored(itemId, stored, function (storedData){
				ItemStoredEdit.setupEditForm(buttonPressed, itemId, storedData);
				Main.processStop();
			});
		}
		console.log("Setting up stored edit form for stored item: ", stored);
		ModalHelpers.setReturnModal(ItemStoredEdit.modal, buttonPressed);
		ItemStoredEdit.resetForm();

		//TODO:: item info display

		ItemStoredEdit.form.attr("action", Rest.passRoot + "/inventory/item/"+itemId+"/stored/"+stored.id);

		let inputs = await StoredFormInput.getStoredInputs(stored.type, stored, null, true);
		ItemStoredEdit.form.append(inputs);

		Main.processStop();
	}
}

ItemStoredEdit.form.on("submit", async function (e) {
	e.preventDefault();
	console.log("Stored item edit form submitted.");

	let updateData = {};
	StoredFormInput.dataFromInputs(updateData, ItemStoredEdit.form);

	console.debug("Stored item update data: ", updateData);

	Rest.call({
		method: "PUT",
		url: ItemStoredEdit.form.attr("action"),
		data: updateData,
		failMessagesDiv: ItemStoredEdit.formMessages,
		done: async function(){
			console.log("Successfully updated stored item.");
			PageMessages.reloadPageWithMessage("Updated stored item successfully!", "success", "Success!");
		}
	});
});