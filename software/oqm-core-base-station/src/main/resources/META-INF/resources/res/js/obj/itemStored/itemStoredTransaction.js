const ItemStoredTransaction = {

	// http://localhost:8080/api/passthrough/inventory/item/673c68565986ac44629caf6c/stored/transact
	//                      /api/passthrough/inventory/item/{itemId}                /stored/transact
	submitTransaction: async function (itemId, transaction, transactionModal) {
		console.log("Submitting transaction for item " + itemId + ":", transaction);
		Rest.call({
			spinnerContainer: transactionModal.find("modal-body")[0],
			failMessagesDiv: transactionModal.find("messages")[0],
			method: "PUT",
			url: Rest.passRoot + "/inventory/item/" + itemId + "/stored/transact",
			data: transaction,
			done: function (appliedTransaction) {
				PageMessages.reloadPageWithMessage("Transaction Successful!", "success", "Success!");
			}
		});
	},
	ModalUtils: {
		getTransactionSelectButton: function () {
			//TODO
		},
		getTransactionSelectDropdown: function (
			itemId = null,
			stored = null,
			{
				buttonText = true,
				showAllTransactions = false,
				showAddTransaction = false,
				showSubtractTransaction = false,
				showTransferTransaction = false,
				showCheckinTransaction = false,
				showCheckoutTransaction = false,
				showSetTransaction = false,
			}
		) {
			if (stored != null) {
				if (itemId == null) {
					itemId = stored.item;
				}
				//TODO:: not show based on stored
			}
			let output = $('<div class="dropdown transact-dropdown">\n' +
				'  <button class="btn btn-secondary dropdown-toggle transactDropdown" type="button" data-bs-toggle="dropdown" aria-expanded="false">\n' +
				'    ' + Icons.transaction + (buttonText ? " Transact" : "") + '\n' +
				'  </button>\n' +
				'  <ul class="dropdown-menu">\n' +
				'  </ul>\n' +
				'</div>');
			let menu = output.find(".dropdown-menu");

			if (showAllTransactions || showAddTransaction) {
				menu.append($('<li><button class="dropdown-item transactDropdownAdd" type="button" onclick="ItemStoredTransaction.Add.setupForm(\'' + itemId + '\', \'' + stored.id + '\', this);" data-bs-toggle="modal" data-bs-target="#itemStoredTransactionAddModal">' +
					Icons.addTransaction + ' Add' +
					'</button></li>'));
			}
			//TODO:: modal js
			if (showAllTransactions || showSubtractTransaction) {
				menu.append($('<li><button class="dropdown-item transactDropdownSubtract" type="button">' + Icons.subtractTransaction + ' Subtract</button></li>'));
			}
			//TODO:: modal js
			if (showAllTransactions || showCheckinTransaction) {
				menu.append($('<li><button class="dropdown-item transactDropdownCheckin" type="button">' + Icons.checkinTransaction + ' Checkin</button></li>'));
			}
			//TODO:: modal js
			if (showAllTransactions || showCheckoutTransaction) {
				menu.append($('<li><button class="dropdown-item transactDropdownCheckout" type="button">' + Icons.checkoutTransaction + ' Checkout</button></li>'));
			}
			//TODO:: modal js
			if (showAllTransactions || showTransferTransaction) {
				menu.append($('<li><button class="dropdown-item transactDropdownTransfer" type="button">' + Icons.transferTransaction + ' Transfer</button></li>'));
			}
			//TODO:: modal js
			if (showAllTransactions || showSetTransaction) {
				menu.append($('<li><button class="dropdown-item transactDropdownSet" type="button">' + Icons.setTransaction + ' Set</button></li>'));
			}

			return output;
		},
		getAddTransactionButton: function (itemId = null, storedId = null) {
			let output = $(
				'<button type="button" class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#itemStoredTransactionAddModal" onclick="ItemStoredTransaction.Add.setupForm(' + itemId + ', ' + storedId + ', this);">' +
				Icons.addTransaction + ' Add' +
				'</button>'
			);
			return output;
		},
		getSubtractTransactionButton: function (itemId = null, storedId = null) {
			//TODO:: update onclick
			let output = $(
				'<button type="button" class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#itemStoredTransactionAddModal" onclick="ItemStoredTransaction.Add.setupForm(' + itemId + ', ' + storedId + ', this);">' +
				Icons.subtractTransaction + ' Subtract' +
				'</button>'
			);
			return output;
		},
		getCheckinTransactionButton: function (itemId = null, storedId = null) {
			//TODO:: update onclick
			let output = $(
				'<button type="button" class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#itemStoredTransactionAddModal" onclick="ItemStoredTransaction.Add.setupForm(' + itemId + ', ' + storedId + ', this);">' +
				Icons.checkinTransaction + ' Checkin' +
				'</button>'
			);
			return output;
		},
		getCheckoutTransactionButton: function (itemId = null, storedId = null) {
			//TODO:: update onclick
			let output = $(
				'<button type="button" class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#itemStoredTransactionAddModal" onclick="ItemStoredTransaction.Add.setupForm(' + itemId + ', ' + storedId + ', this);">' +
				Icons.checkoutTransaction + ' Checkout' +
				'</button>'
			);
			return output;
		},
		getTransferTransactionButton: function (itemId = null, storedId = null) {
			//TODO:: update onclick
			let output = $(
				'<button type="button" class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#itemStoredTransactionAddModal" onclick="ItemStoredTransaction.Add.setupForm(' + itemId + ', ' + storedId + ', this);">' +
				Icons.transferTransaction + ' Transfer' +
				'</button>'
			);
			return output;
		},
		getSetTransactionButton: function (itemId = null, storedId = null) {
			//TODO:: update onclick
			let output = $(
				'<button type="button" class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#itemStoredTransactionAddModal" onclick="ItemStoredTransaction.Add.setupForm(' + itemId + ', ' + storedId + ', this);">' +
				Icons.setTransaction + ' Set Amount' +
				'</button>'
			);
			return output;
		}
	},
	StoredFormUtils: {
		getBasicInputs(stored) {
			//TODO:: update to use barcode input
			//TODO:: update these
			let output = $(
				'<div class="commonStoredFormElements">' +
				'<div class="mb-3 ">\n' +
				'    <label class="form-label">Barcode</label>\n' +
				'    <div class="input-group">\n' +
				'        <input type="text" class="form-control storedBarcodeInput" name="barcode" placeholder="UPC, ISBN...">\n' +
				'    </div>\n' + '</div>\n' + '<div class="mb-3 ">\n' +
				'    <label class="form-label">Condition Percentage</label>\n' +
				'    <div class="input-group">\n' +
				'        <input type="number" max="100" min="0" step="any" class="form-control storedConditionPercentageInput" name="condition">\n' +
				//TODO:: better label of better to worse
				'        <span class="input-group-text" id="addon-wrapping">%</span>\n' +
				//TODO:: better label of better to worse
				'    </div>\n' + '</div>\n' + '<div class="mb-3">\n' +
				'    <label class="form-label">Condition Details</label>\n' +
				'    <textarea class="form-control" name="conditionDetails"></textarea>\n' +
				'</div>\n' +
				'<div class="mb-3">\n' +
				'    <label class="form-label">Expires</label>\n' +
				'    <input type="datetime-local" class="form-control storedExpiredInput" name="expires">\n' +
				//TODO:: note to leave blank if not applicable
				'</div>\n' + //TODO:: move these templates to js calls
				// imageInputTemplate.html() +

				//TODO:: show kw/att on same row. images too?
				PageComponents.Inputs.keywords +
				PageComponents.Inputs.attribute +
				'</div>\n'
			);

			//TODO:: populate from stored

			return output;
		},
		getAmountInputs: async function (item, stored) {
			console.log("Getting amount inputs");
			let output = $(
				'<div class="amountStoredFormElements">' +
				'<label class="form-label">Amount:</label>\n' +
				'<div class="input-group mt-2 mb-3">\n' +
				'     <input type="number" class="form-control amountStoredValueInput" name="amountStored" placeholder="Value" value="0.00" min="0.00" step="any" required>\n' +
				'     <select class="form-select amountStoredUnitInput unitInput" name="amountStoredUnit"></select>\n' +
				'</div>' +
				'</div>'
			);

			//TODO:: selected unit from stored
			let unitOps = await UnitUtils.getCompatibleUnitOptions(item.unit.string);
			output.find(".unitInput").append(unitOps);

			return output;
		},
		getUniqueInputs(stored) {
			let output = $('<div class="uniqueStoredFormInputs"></div>');
			//TODO:: make inputs

			return output;
		},
		getStoredInputs: async function (item, stored) {
			let output = $('<div class="storedInputs"></div>');

			StorageTypeUtils.runForStoredType(item, async function () {
				let amountInputs = await ItemStoredTransaction.StoredFormUtils.getAmountInputs(item, stored);
				output.append(amountInputs);
			}, function () {
				output.append(ItemStoredTransaction.StoredFormUtils.getUniqueInputs(item, stored));
			});

			output.append(this.getBasicInputs(stored));

			return output;
		}

	}, Add: {
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
		toBlockRadio: $("#itemStoredTransactionAddFormToBlockRadio"),

		toStoredInputContainer: $("#itemStoredTransactionAddFormToStoredInputContainer"),
		toStoredInput: $("#itemStoredTransactionAddFormToStoredInput"),
		toStoredRadio: $("#itemStoredTransactionAddFormToStoredRadio"),

		inputsContainer: $("#itemStoredTransactionAddFormInputsContainer"),

		ableToInputs(inputsContainerJq, disabled = true, readonly = false, clearRadios = true) {
			let radioInputs = inputsContainerJq.find('input[name="toInput"]');
			let inputs = inputsContainerJq.find(".card-body").find('input, select');
			let cardBody = inputsContainerJq.find(".card-body");

			radioInputs.prop("disabled", disabled);
			radioInputs.prop("readonly", readonly);
			if (clearRadios) {
				radioInputs.prop("checked", false);
			}

			if (disabled || !radioInputs.is(":checked")) {
				console.log("Adding opacity to ", inputsContainerJq.attr("id"))
				cardBody.addClass("opacity-25");
			} else {
				console.log("clearing opacity.")
				cardBody.removeClass("opacity-25");
			}

			inputs.prop("disabled", disabled);
			inputs.prop("readonly", radioInputs.prop("readonly"));
		},

		/**
		 * Changes inputs based on state of type, and to inputs
		 * @param item
		 */
		updateInputs: async function (item = null) {
			if (item == null) {
				item = this.itemIdInput.val();
			}
			if (typeof item === "string" || (item instanceof String)) {
				Getters.InventoryItem.get(item, this.updateInputs);
				return;
			}
			console.log("Updating add transaction form inputs.");

			ItemStoredTransaction.Add.inputsContainer.text("");
			let storedInputs = await ItemStoredTransaction.StoredFormUtils.getStoredInputs(item, null);
			ItemStoredTransaction.Add.inputsContainer.append(storedInputs);

			StorageTypeUtils.runForType(
				item,
				async function () {
					console.log("Updating inputs for Bulk item");
					ItemStoredTransaction.Add.ableToInputs(ItemStoredTransaction.Add.toStoredInputContainer, false, false, false);
					ItemStoredTransaction.Add.ableToInputs(ItemStoredTransaction.Add.toBlockInputContainer, false, false, false);

					let type = ItemStoredTransaction.Add.typeInput.val();
					switch (type) {
						case "ADD_AMOUNT":
							console.debug("Removing common elements.");
							ItemStoredTransaction.Add.inputsContainer.find(".commonStoredFormElements").remove();
							break;
						case "ADD_WHOLE":
							ItemStoredTransaction.Add.ableToInputs(ItemStoredTransaction.Add.toStoredInputContainer, true, false, false);
							//TODO:: disable toBlock values with something already stored in it
							break;
					}
				}, function () {
					ItemStoredTransaction.Add.ableToInputs(ItemStoredTransaction.Add.toStoredInputContainer, false, false, false);
					ItemStoredTransaction.Add.ableToInputs(ItemStoredTransaction.Add.toBlockInputContainer, false, false, false);

					let type = ItemStoredTransaction.Add.typeInput.val();
					switch (type) {
						case "ADD_AMOUNT":
							console.debug("Removing common elements.");
							ItemStoredTransaction.Add.inputsContainer.find(".commonStoredFormElements").remove();
							break;
						case "ADD_WHOLE":
							ItemStoredTransaction.Add.ableToInputs(ItemStoredTransaction.Add.toStoredInputContainer, true, false, true);
							break;
					}
				}, function () {
					ItemStoredTransaction.Add.ableToInputs(ItemStoredTransaction.Add.toStoredInputContainer, true, false, false);
					ItemStoredTransaction.Add.ableToInputs(ItemStoredTransaction.Add.toBlockInputContainer, false, false, false);
				}, function () {
					ItemStoredTransaction.Add.ableToInputs(ItemStoredTransaction.Add.toStoredInputContainer, true, false, false);
					ItemStoredTransaction.Add.ableToInputs(ItemStoredTransaction.Add.toBlockInputContainer, false, false, false);
				});
			console.debug("Done updating inputs.");
		},
		resetForm: function (changeItemRelated = true) {
			console.log("Resetting item stored add transaction form.");
			this.form.trigger("reset");
			this.storedIdInput.val("");

			if (changeItemRelated) {
				this.itemIdInput.val("");
				this.itemInputContainer.hide(0);
				this.itemNameInput.val("");
				this.itemNameInput.prop("disabled", true);
				this.itemClearButton.prop("disabled", true);
				this.itemSearchButton.prop("disabled", true);
				this.itemDisplayContainer.hide(0);
				this.itemDisplayName.text('');
			}

			this.typeInputContainer.hide();
			ItemStoredTransaction.Add.typeInput.prop("disabled", false);

			this.ableToInputs(this.toStoredInputContainer);
			this.toStoredInputContainer.hide(0);
			this.ableToInputs(this.toBlockInputContainer);
			this.toBlockInputContainer.hide(0);

			this.toStoredInput.html("");
			this.toBlockInput.html("");
		},
		setupFormForItem: async function (itemId) {
			Main.processStart();
			console.log("Setting up item stored add form for item ", itemId);
			this.resetForm(false);
			//TODO:: populate stored dropdown
			let promises = [];
			promises.push(Getters.InventoryItem.get(itemId, async function (item) {
				let itemPromises = [];

				ItemStoredTransaction.Add.itemIdInput.val(item.id);
				ItemStoredTransaction.Add.itemNameInput.val(item.name);
				ItemStoredTransaction.Add.itemDisplayName.text(item.name);

				item.storageBlocks.forEach(function (blockId) {
					let blockOp = $("<option></option>");
					blockOp.val(blockId);
					blockOp.text(blockId);
					itemPromises.push(getStorageBlockLabel(blockId, function (blockLabel) {
						blockOp.text(blockLabel);
					}));
					ItemStoredTransaction.Add.toBlockInput.append(blockOp);
				});

				StorageTypeUtils.runForType(
					item,
					function () {
						ItemStoredTransaction.Add.typeInputContainer.show(0);
						ItemStoredTransaction.Add.typeInput.val("ADD_AMOUNT");
						ItemStoredTransaction.Add.typeInput.prop("disabled", true);
						ItemStoredTransaction.Add.toBlockInputContainer.show(0);
						ItemStoredTransaction.Add.toBlockRadio.prop("checked", true);
					},
					function () {
						ItemStoredTransaction.Add.typeInputContainer.show(0);
						ItemStoredTransaction.Add.toBlockInputContainer.show(0);
						ItemStoredTransaction.Add.toStoredInputContainer.show(0);
					},
					function () {
						ItemStoredTransaction.Add.typeInputContainer.show(0);
						ItemStoredTransaction.Add.typeInput.val("ADD_WHOLE");
						ItemStoredTransaction.Add.typeInput.prop("disabled", true);
						ItemStoredTransaction.Add.toBlockInputContainer.show(0);
					},
					function () {
						ItemStoredTransaction.Add.typeInputContainer.show(0);
						ItemStoredTransaction.Add.typeInput.val("ADD_WHOLE");
						ItemStoredTransaction.Add.typeInput.prop("disabled", true);
						ItemStoredTransaction.Add.toBlockInputContainer.show(0);
					}
				);
				itemPromises.push(ItemStoredTransaction.Add.updateInputs(item));
				await Promise.all(itemPromises);
			}));
			promises.push(Getters.StoredItem.getStoredForItem(itemId, async function (storedResults) {
				console.log("Processing stored for item into selects: ", storedResults.numResults);
				let storedPromises = [];
				storedResults.results.forEach(function (curStored) {
					let newSelect = $('<option>...</option>');
					newSelect.val(curStored.id);

					storedPromises.push(Getters.StoredItem.getLabelForStored(curStored, function (label) {
						newSelect.text(label);
						ItemStoredTransaction.Add.toStoredInput.append(newSelect);
					}));
				});
				await Promise.all(storedPromises);
			}));

			await Promise.all(promises);
			console.log("Finished setting up add transaction form .");
			Main.processStop();
		},
		setupForm: async function (itemId = null, preselectedStoredId = null, buttonElement = null) {
			//TODO:: do something wiht preselected stored
			console.log("Setting up item stored add transaction form for item", itemId);
			ModalHelpers.setReturnModal(this.modal, buttonElement);
			this.resetForm();
			if (itemId != null) {
				console.log("Given an item, keeping inputs disabled.");
				this.itemDisplayContainer.show();
				await this.setupFormForItem(itemId);
			} else {
				console.log("Enabling for searching for item.");
				this.itemInputContainer.show();
				this.itemNameInput.prop("disabled", false);
				this.itemClearButton.prop("disabled", false);
				this.itemSearchButton.prop("disabled", false);
			}
			if (preselectedStoredId != null) {
				//TODO:: setup form for stored
			}
		},
		submitForm: async function () {
			console.log("Submitting Add Transaction form");

			let data = {
				transactionType: this.typeInput.val(),
			}

			//determine "to" value
			if (this.toStoredRadio.is(":checked")) {
				console.debug("Going to stored.");
				data.toStored = "";//TODO:: when we have the input implemented
			}
			if (this.toBlockRadio.is(":checked")) {
				console.debug("Going to block.");
				data.toBlock = this.toBlockInput.val();
			}

			switch (data.transactionType) {
				case "ADD_AMOUNT":
					console.debug("Amount fields present.");
					data.amount = UnitUtils.getQuantityFromInputs(this.inputsContainer);
					break;
				case "ADD_WHOLE":
					data.toAdd = {};
					let toAddFieldsTo = data.toAdd;

					let amtFormElements = this.inputsContainer.find(".amountStoredFormElements");
					if (amtFormElements.length) {
						console.debug("Amount fields present.");
						toAddFieldsTo.amount = UnitUtils.getQuantityFromInputs(this.inputsContainer);
						toAddFieldsTo.type = "AMOUNT";
					} else {
						toAddFieldsTo.type = "UNIQUE";
					}
					let commFormElements = this.inputsContainer.find(".commonStoredFormElements");
					if (commFormElements) {
						console.debug("Common fields present.");

						toAddFieldsTo["barcode"] = commFormElements.find('input[name="barcode"]').val();
						toAddFieldsTo["condition"] = commFormElements.find('input[name="condition"]').val();
						toAddFieldsTo["conditionDetails"] = commFormElements.find('input[name="conditionDetails"]').val();
						toAddFieldsTo["expires"] = commFormElements.find('input[name="expires"]').val();
						toAddFieldsTo["item"] = ItemStoredTransaction.Add.itemIdInput.val();
						toAddFieldsTo["storageBlock"] = ItemStoredTransaction.Add.toBlockInput.val();
						KeywordAttEdit.addKeywordAttData(toAddFieldsTo, commFormElements.find(".keywordInputDiv"), commFormElements.find(".attInputDiv"));
					}
					break;
			}

			await ItemStoredTransaction.submitTransaction(
				this.itemIdInput.val(),
				data,
				this.modal
			);
		},
		/**
		 * Handler for submitting the add transaction form.
		 * Wrapper for submitForm, in order to enable the use of "this" in that method.
		 * @param event
		 * @returns {Promise<void>}
		 */
		submitFormHandler: async function (event) {
			event.preventDefault();
			await ItemStoredTransaction.Add.submitForm();
		}
	}, Checkin: {
		//TODO
	}, Checkout: {
		//TODO
	}, Set: {
		//TODO
	}, Subtract: {
		//TODO
	}, Transfer: {
		//TODO
	}
};

ItemStoredTransaction.Add.form.on("submit", ItemStoredTransaction.Add.submitFormHandler);