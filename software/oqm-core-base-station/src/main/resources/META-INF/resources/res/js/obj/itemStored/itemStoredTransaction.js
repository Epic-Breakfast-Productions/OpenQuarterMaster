const ItemStoredTransaction = {

	submitTransaction: async function (itemId, transaction) {
		//TODO
	},

	StoredFormUtils: {
		getBasicInputs(stored) {
			//TODO:: update to use barcode input
			//TODO:: update these
			let output = $('<div class="storedEditCommonFields">' +
				'<div class="mb-3 ">\n' +
				'    <label class="form-label">Barcode</label>\n' +
				'    <div class="input-group">\n' +
				'        <input type="text" class="form-control storedBarcodeInput" name="barcode" placeholder="UPC, ISBN...">\n' +
				'    </div>\n' +
				'</div>\n' +
				'<div class="mb-3 ">\n' +
				'    <label class="form-label">Condition Percentage</label>\n' +
				'    <div class="input-group">\n' +
				'        <input type="number" max="100" min="0" step="any" class="form-control storedConditionPercentageInput" name="condition">\n' + //TODO:: better label of better to worse
				'        <span class="input-group-text" id="addon-wrapping">%</span>\n' + //TODO:: better label of better to worse
				'    </div>\n' +
				'</div>\n' +
				'<div class="mb-3">\n' +
				'    <label class="form-label">Condition Details</label>\n' +
				'    <textarea class="form-control" name="conditionDetails"></textarea>\n' +
				'</div>\n' +
				'<div class="mb-3">\n' +
				'    <label class="form-label">Expires</label>\n' +
				'    <input type="date" class="form-control storedExpiredInput" name="expires">\n' +
				//TODO:: note to leave blank if not applicable
				'</div>\n' +
				//TODO:: move these templates to js calls
				// imageInputTemplate.html() +

				//TODO:: show kw/att on same row. images too?
				PageComponents.Inputs.keywords +
				PageComponents.Inputs.attribute +
				'</div>\n'
			);

			//TODO:: populate from stored

			return output;
		},
		getAmountInputs: function (item, stored) {
			console.log("Getting amount inputs");
			//TODO:: update compatible unit tools
			let output = $('<div class="amountStoredFormElements">' +
				'<label class="form-label">Amount:</label>\n' +
				'<div class="input-group mt-2 mb-3">\n' +
				'     <input type="number" class="form-control amountStoredValueInput" name="amountStored" placeholder="Value" value="0.00" min="0.00" step="any" required>\n' +
				'     <select class="form-select amountStoredUnitInput unitInput" name="amountStoredUnit">' + ItemAddEdit.compatibleUnitOptions + '</select>\n' + //TODO:: populate
				'</div>' +
				'</div>');
			// UnitUtils.getUnitOptions()

			//TODO:: populate units
			//TODO:: populate from stored

			return output;
		},
		getUniqueInputs(stored) {
			let output = $('<div class="uniqueStoredFormInputs"></div>');
			//TODO:: make inputs

			return output;
		},
		getStoredInputs(item, stored) {
			let output = $('<div class="storedInputs"></div>');

			StorageTypeUtils.runForStoredType(item,
				function () {
					output.append(
						ItemStoredTransaction.StoredFormUtils.getAmountInputs(stored)
					);
				},
				function () {
					output.append(
						ItemStoredTransaction.StoredFormUtils.getUniqueInputs(stored)
					);
				}
			);

			output.append(this.getBasicInputs(stored));

			return output;
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

		updateInputs: function (item = null) {
			if (item == null) {
				item = this.itemIdInput.val();
			}
			if (typeof item === "string" || (item instanceof String)) {
				Getters.InventoryItem.get(item, this.updateInputs);
				return;
			}

			ItemStoredTransaction.Add.inputsContainer.text("");
			ItemStoredTransaction.Add.inputsContainer.append(
				ItemStoredTransaction.StoredFormUtils.getStoredInputs(item, null)
			);
			if (
				!ItemStoredTransaction.Add.typeInput.is(":hidden") &&
				ItemStoredTransaction.Add.typeInput.val() === "ADD_AMOUNT"
			) {
				ItemStoredTransaction.Add.inputsContainer.find(".storedEditCommonFields")
					.hide();
			}
		},
		resetForm: function (changeItemRelated = true) {
			console.log("Resetting item stored add transaction form.");
			this.form.trigger("reset");
			this.storedIdInput.val("");

			if (changeItemRelated) {
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
		setupFormForItem: async function (itemId) {
			console.log("Setting up item stored add form for item ", itemId);
			this.resetForm(false);
			Getters.InventoryItem.get(itemId, async function (item) {
				let promises = [];

				ItemStoredTransaction.Add.itemIdInput.val(item.id);
				ItemStoredTransaction.Add.itemNameInput.val(item.name);
				ItemStoredTransaction.Add.itemDisplayName.text(item.name);

				item.storageBlocks.forEach(function (blockId) {
					let blockOp = $("<option></option>");
					blockOp.val(blockId);
					blockOp.text(blockId);
					promises.push(getStorageBlockLabel(blockId, function (blockLabel) {
						blockOp.text(blockLabel);
					}));
					ItemStoredTransaction.Add.toBlockInput.append(blockOp);
				});

				StorageTypeUtils.runForType(
					item,
					function () {
						//TODO:: if something exists in storage block, disable whole stored input
						ItemStoredTransaction.Add.typeInputContainer.show();
						ItemStoredTransaction.Add.updateInputs(item);
					},
					function () {
						ItemStoredTransaction.Add.typeInputContainer.show();
						ItemStoredTransaction.Add.updateInputs(item);
					},
					function () {
						ItemStoredTransaction.Add.updateInputs(item);
					},
					function () {
						//TODO
					}
				);
				await Promise.all(promises);
			});
		},
		setupForm: async function (itemId = null, preselectedStored = null, buttonElement = null) {
			console.log("Setting up item stored add transaction form for item", itemId);
			ModalHelpers.setReturnModal(this.modal, buttonElement);
			this.resetForm();
			if (itemId != null) {
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