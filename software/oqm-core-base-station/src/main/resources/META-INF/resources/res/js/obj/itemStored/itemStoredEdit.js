
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

		let inputs = await StoredFormInput.getStoredInputs(stored.type, stored, null, true);
		ItemStoredEdit.form.append(inputs);

		Main.processStop();
	}
}