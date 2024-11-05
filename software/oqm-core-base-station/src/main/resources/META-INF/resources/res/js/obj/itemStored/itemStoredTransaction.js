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
	Add: {
		modal: $("#itemStoredTransactionAddModal"),
		messages: $("#itemStoredTransactionAddMessages"),
		form: $("#itemStoredTransactionAddForm"),

		idInput: $("#itemStoredTransactionAddFormItemIdInput"),
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
			this.form.trigger("reset");
			this.idInput.val("");
			this.typeInputContainer.hide();
			this.toStoredInputContainer.hide();
			this.toStoredInput.html("");
			this.toBlockInput.html("");
		},
		setupForm(itemId, preselectedStored = null, buttonElement = null) {
			console.log("Setting up item stored add transaction form for item", itemId);
			ModalHelpers.setReturnModal(this.modal, buttonElement);
			this.resetForm();
			Getters.InventoryItem.get(itemId, async function (item) {
				let promises = [];
				ItemStoredTransaction.Add.resetForm();
				ItemStoredTransaction.Add.idInput.val(itemId);

				item.storageBlocks.forEach(function(blockId){
					let blockOp = $("<option></option>");
					blockOp.val(blockId);
					blockOp.text(blockId);
					promises.push(getStorageBlockLabel(blockId, function(blockLabel){
						blockOp.text(blockLabel);
					}));
					ItemStoredTransaction.Add.toBlockInput.append(blockOp);
				});

				StorageTypeUtils.runForType(
					item,
					function () {
						ItemStoredTransaction.Add.typeInputContainer.show();
						//TODO::
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
				await Promise.all(promises);
			});
		}
	},
	Checkin: {
		//TODO
	},
	Checkout: {
		//TODO
	},
	Set: {
		//TODO
	},
	Subtract: {
		//TODO
	},
	Transfer: {
		//TODO
	}
};