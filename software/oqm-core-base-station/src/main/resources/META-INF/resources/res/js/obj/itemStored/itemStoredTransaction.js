const ItemStoredTransaction = {

	submitTransaction: async function (itemId, transaction) {
		//TODO
	},

	storedFormUtils: {
		getBasicInputs(stored){
			
		},
		getAmountInputs: async function(item, stored){
			let output = $('' +
				'<div class="mb-3">\n' +
				'    <label class="form-label">To Stored</label>\n' +
				'    <select class="form-select" id="">\n' +
				'    </select>\n' +
				'</div>' +
				'');
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

		storedIdInput: $("#itemStoredTransactionAddFormStoredIdInput"),
		itemInputContainer: $("#itemStoredTransactionAddFormItemInputContainer"),
		itemIdInput: $("#itemStoredTransactionAddFormItem-itemInputId"),
		itemSearchButton: $("#itemStoredTransactionAddFormItem-itemInputSearchButton"),
		itemNameInput: $("#itemStoredTransactionAddFormItem-itemInputName"),
		itemClearButton: $("#itemStoredTransactionAddFormItem-itemInputClearButton"),

		itemDisplayContainer: $("#itemStoredTransactionAddFormItemDisplayContainer"),
		itemDisplayName: $("#itemStoredTransactionAddFormItemDisplayName"),






		typeInputContainer: $("#itemStoredTransactionAddFormTypeInputContainer"),
		typeInput: $("#itemStoredTransactionAddFormTypeInput"),
		toBlockInputContainer: $("#itemStoredTransactionAddFormToBlockInputContainer"),
		toBlockInput: $("#itemStoredTransactionAddFormToBlockInput"),
		toStoredInputContainer: $("#itemStoredTransactionAddFormToStoredInputContainer"),
		toStoredInput: $("#itemStoredTransactionAddFormToStoredInput"),
		inputsContainer: $("#itemStoredTransactionAddFormInputsContainer"),

		updateInputs: function(){

		},
		resetForm: function (changeItemRelated = true) {
			console.log("Resetting item stored add transaction form.");
			this.form.trigger("reset");
			this.storedIdInput.val("");

			if(changeItemRelated) {
				this.itemIdInput.val("");
				this.itemInputContainer.hide();
				this.itemNameInput.val("");
				this.itemNameInput.prop("disabled", true);
				this.itemClearButton.prop("disabled", true);
				this.itemSearchButton.prop("disabled", true);
				this.itemDisplayContainer.hide();
				this.itemDisplayName.text('');
			}

			this.typeInputContainer.hide();
			this.toStoredInputContainer.hide();
			this.toStoredInput.html("");
			this.toBlockInput.html("");
		},
		setupFormForItem: async function(itemId){
			console.log("Setting up item stored add form for item ", itemId);
			this.resetForm(false);
			Getters.InventoryItem.get(itemId, async function (item) {
				let promises = [];

				ItemStoredTransaction.Add.itemIdInput.val(item.id);
				ItemStoredTransaction.Add.itemNameInput.val(item.name);
				ItemStoredTransaction.Add.itemDisplayName.text(item.name);

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
		},
		setupForm: async function(itemId = null, preselectedStored = null, buttonElement = null) {
			console.log("Setting up item stored add transaction form for item", itemId);
			ModalHelpers.setReturnModal(this.modal, buttonElement);
			this.resetForm();
			if(itemId != null){
				console.log("Given an item, keeping inputs disabled.");
				this.itemDisplayContainer.show();
				await this.setupFormForItem(itemId, false);
			} else {
				console.log("Enabling for searching for item.");
				this.itemInputContainer.show();
				this.itemNameInput.prop("disabled", false);
				this.itemClearButton.prop("disabled", false);
				this.itemSearchButton.prop("disabled", false);
			}
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