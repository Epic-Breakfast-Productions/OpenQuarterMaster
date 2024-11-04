const ItemStoredTransaction = {

	submitTransaction: async function (itemId, transaction) {
		//TODO
	},

	storedFormUtils: {
		getBasicInputs(stored){

		},
		getAmountInputs(stored){

		},
		getUniqueInputs(stored){

		},
		getStoredInputs(stored){

		}
	},
	add: {
		modal: $("#itemStoredTransactionAddModal"),
		messages: $("#itemStoredTransactionAddMessages"),

		typeInputContainer: $("#itemStoredTransactionAddFormTypeInputContainer"),
		typeInput: $("#itemStoredTransactionAddFormTypeInput"),
		toBlockInputContainer: $("#itemStoredTransactionAddFormToBlockInputContainer"),
		toBlockInput: $("#itemStoredTransactionAddFormToBlockInput"),
		toStoredInputContainer: $("#itemStoredTransactionAddFormToStoredInputContainer"),
		toStoredInput: $("#itemStoredTransactionAddFormToStoredInput"),
		amountContainer: $("#itemStoredTransactionAddFormAmountContainer"),
		storedContainer: $("#itemStoredTransactionAddFormStoredContainer"),

		resetForm: function () {
			console.log("Resetting item stored add transaction form.");
		},
		setupForm(itemId, preselectedStored = null, buttonElement = null) {
			console.log("Setting up item stored add transaction form for item ", itemId);
			ModalHelpers.setReturnModal(this.modal, buttonElement);
			this.resetForm();
			Getters.InventoryItem.get(itemId, function (item) {
				//TODO:: setup

				StorageTypeUtils.runForType(
					item,
					function () {
						//TODO
					},
					function () {
						//TODO
					},
					function () {
						//TODO
					},
					function () {
						//TODO
					}
				);
			});
		}
	},
	checkin: {
		//TODO
	},
	checkout: {
		//TODO
	},
	set: {
		//TODO
	},
	subtract: {
		//TODO
	},
	transfer: {
		//TODO
	}
};