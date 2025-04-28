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
	ModalButtons: {
		getTransactionSelectButton: function () {
			//TODO
		},
		getTransactionSelectDropdown: async function (
			item = null,
			stored = null,
			{
				buttonText = true,
				showAddTransaction = true,
				showSubtractTransaction = true,
				showTransferTransaction = true,
				showCheckinTransaction = true,
				showCheckoutTransaction = true,
				showSetTransaction = true,
			}
		) {

			let query = new URLSearchParams();

			if(item != null){
				if (typeof item === 'string' || item instanceof String){
					query.set("item", item);
				} else {
					query.set("item", item.id);
				}
			}
			if(stored != null){
				if (typeof stored === 'string' || stored instanceof String){
					query.set("stored", stored);
				} else {
					query.set("stored", stored.id);
				}
			}
			//TODO:: add flags to query

			let output;
			await Rest.call({
				url: Rest.componentRoot + "/itemStoredTransaction/dropdown?" + query.toString(),
				returnType:"html",
				done: function (buttonData){
					output = $(buttonData);
				}
			});

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
			Main.processStart("Update add transaction inputs.");
			if (item == null) {
				item = this.itemIdInput.val();
			}
			if (typeof item === "string" || (item instanceof String)) {
				Getters.InventoryItem.get(item, this.updateInputs);
				Main.processStop("Update add transaction inputs. (recursive call)");
				return;
			}
			console.log("Updating add transaction form inputs.");

			ItemStoredTransaction.Add.inputsContainer.text("");
			let storedInputs = await StoredFormInput.getStoredInputs(StorageTypeUtils.storageToStoredType(item), null, item, false);
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
							ItemStoredTransaction.Add.toStoredInputContainer.show(0);
							break;
						case "ADD_WHOLE":
							ItemStoredTransaction.Add.ableToInputs(ItemStoredTransaction.Add.toStoredInputContainer, true, false, true);
							ItemStoredTransaction.Add.toStoredInputContainer.hide(0);
							ItemStoredTransaction.Add.toBlockRadio.prop("checked", true);
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
			Main.processStop("Update add transaction inputs.");
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
						ItemStoredTransaction.Add.toBlockRadio.prop("checked", true);
					},
					function () {
						ItemStoredTransaction.Add.typeInputContainer.show(0);
						ItemStoredTransaction.Add.typeInput.val("ADD_WHOLE");
						ItemStoredTransaction.Add.typeInput.prop("disabled", true);
						ItemStoredTransaction.Add.toBlockInputContainer.show(0);
						ItemStoredTransaction.Add.toBlockRadio.prop("checked", true);
					},
					function () {
						ItemStoredTransaction.Add.typeInputContainer.show(0);
						ItemStoredTransaction.Add.typeInput.val("ADD_WHOLE");
						ItemStoredTransaction.Add.typeInput.prop("disabled", true);
						ItemStoredTransaction.Add.toBlockInputContainer.show(0);
						ItemStoredTransaction.Add.toBlockRadio.prop("checked", true);
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
				this.itemNameInput.prop("disabled", false);
				this.itemClearButton.prop("disabled", false);
				this.itemSearchButton.prop("disabled", false);
				this.itemInputContainer.show();
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
					data["toBlock"] = ItemStoredTransaction.Add.toBlockInput.val();
					StoredFormInput.dataFromInputs(data.toAdd, this.inputsContainer);
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
	},
	Checkin: {
		modal: $("#itemStoredTransactionCheckinModal"),
		messages: $("#itemStoredTransactionCheckinMessages"),
		form: $("#itemStoredTransactionCheckinForm"),

		resetForm(){
			//TODO
		},
		setupForm(itemId, storedId, buttonElement){
			console.log("Setting up item stored checkin transaction form for item", itemId);
			ModalHelpers.setReturnModal(this.modal, buttonElement);
			this.resetForm();
			//TODO
		}
	},
	Checkout: {
		modal: $("#itemStoredTransactionCheckoutModal"),
		messages: $("#itemStoredTransactionCheckoutMessages"),
		form: $("#itemStoredTransactionCheckoutForm"),

		resetForm(){
			//TODO
		},
		setupForm(itemId, storedId, buttonElement){
			console.log("Setting up item stored checkout transaction form for item", itemId);
			ModalHelpers.setReturnModal(this.modal, buttonElement);
			this.resetForm();
			//TODO
		}
	},
	Set: {
		modal: $("#itemStoredTransactionSetModal"),
		messages: $("#itemStoredTransactionSetMessages"),
		form: $("#itemStoredTransactionSetForm"),

		storedIdInput: $("#itemStoredTransactionSetFormStoredIdInput"),


		resetForm(){
			//TODO
		},
		setupForm(itemId, storedId, buttonElement){
			console.log("Setting up item stored set transaction form for item", itemId);
			ModalHelpers.setReturnModal(this.modal, buttonElement);
			this.resetForm();
			//TODO
		}
	},
	Subtract: {
		modal: $("#itemStoredTransactionSubtractModal"),
		messages: $("#itemStoredTransactionSubtractMessages"),
		form: $("#itemStoredTransactionSubtractForm"),

		storedIdInput: $("#itemStoredTransactionSubtractFormStoredIdInput"),

		resetForm(){
			//TODO
		},
		setupForm(itemId, storedId, buttonElement){
			console.log("Setting up item stored subtract transaction form for item", itemId);
			ModalHelpers.setReturnModal(this.modal, buttonElement);
			this.resetForm();
			//TODO
		}
	},
	Transfer: {
		modal: $("#itemStoredTransactionTransferModal"),
		messages: $("#itemStoredTransactionTransferMessages"),
		form: $("#itemStoredTransactionTransferForm"),

		itemInputContainer: $("#itemStoredTransactionTransferFormItemInputContainer"),
		itemInputClearButton: $("#itemStoredTransactionTransferFormItemInputContainer.clearButton"),

		itemInfoContainer: $("#itemStoredTransactionTransferItemInfoContainer"),
		itemInfoItemName: $("#itemStoredTransactionTransferItemInfoItemName"),

		storedInfoContainer: $("#itemStoredTransactionTransferInfoContainer"),

		itemIdInput: $("#itemStoredTransactionTransferFormItemIdInput"),
		transactionTypeContainer: $("#itemStoredTransactionTransferFormTypeInputContainer"),
		transactionTypeInput: $("#itemStoredTransactionTransferFormTypeInput"),
		fromBlockContainer: $("#itemStoredTransactionTransferFormFromBlockContainer"),
		fromBlockSelect: $("#itemStoredTransactionTransferFormFromBlockSelect"),
		fromStoredContainer: $("#itemStoredTransactionTransferFormFromStoredContainer"),
		fromStoredSelect: $("#itemStoredTransactionTransferFormFromItemStored-inputGroup"),
		fromStoredItemIdInput: $("#itemStoredTransactionTransferFormFromItemStored-itemStoredInputItemId"),
		fromStoredStoredIdInput: $("#itemStoredTransactionTransferFormFromItemStored-itemStoredInputId"),

		amountInputContainer: $("#itemStoredTransactionTransferFormAmountContainer"),
		amountInputs: $("#itemStoredTransactionTransferFormAmountInputs"),
		amountTransferAllInput: $("#itemStoredTransactionTransferFormAmountTransferAllInput"),

		toBlockContainer: $("#itemStoredTransactionTransferFormToBlockContainer"),
		toBlockSelect: $("#itemStoredTransactionTransferFormToBlockSelect"),
		toStoredContainer: $("#itemStoredTransactionTransferFormToStoredContainer"),
		toStoredSelect: $("#itemStoredTransactionTransferFormToItemStored-inputGroup"),
		toStoredItemIdInput: $("#itemStoredTransactionTransferFormToItemStored-itemStoredInputItemId"),
		toStoredIdInput: $("#itemStoredTransactionTransferFormToItemStored-itemStoredInputId"),

		resetForm: function () {
			ItemStoredTransaction.Transfer.itemInputContainer.hide();
			ItemSearchSelect.clearSearchInput(ItemStoredTransaction.Transfer.itemInputClearButton);

			ItemStoredTransaction.Transfer.itemInfoContainer.hide();
			ItemStoredTransaction.Transfer.itemInfoItemName.text("");

			ItemStoredTransaction.Transfer.storedInfoContainer.hide();
			//TODO

			ItemStoredTransaction.Transfer.messages.text("");
			ItemStoredTransaction.Transfer.itemIdInput.val("");
			ItemStoredTransaction.Transfer.transactionTypeContainer.hide();
			ItemStoredTransaction.Transfer.fromBlockContainer.hide();
			ItemStoredTransaction.Transfer.fromBlockSelect.text("");
			ItemStoredTransaction.Transfer.fromStoredContainer.hide();
			ItemStoredSearchSelect.resetSearchInput(ItemStoredTransaction.Transfer.fromStoredSelect);


			ItemStoredTransaction.Transfer.amountInputContainer.hide();
			ItemStoredTransaction.Transfer.amountInputs.text("");
			ItemStoredTransaction.Transfer.amountTransferAllInput.prop("checked", false);

			ItemStoredTransaction.Transfer.toBlockContainer.hide();
			ItemStoredTransaction.Transfer.toBlockSelect.text("");
			ItemStoredTransaction.Transfer.toStoredContainer.hide();
			ItemStoredSearchSelect.resetSearchInput(ItemStoredTransaction.Transfer.toStoredSelect);
		},
		setupForm: async function (item, stored, buttonElement) {
			Main.processStart();
			ModalHelpers.setReturnModal(this.modal, buttonElement);
			ItemStoredTransaction.Transfer.resetForm();
			if(item == null){
				ItemStoredTransaction.Transfer.itemInputContainer.show();
				Main.processStop();
				return;
			}

			if(typeof item === "string" || (item instanceof String)){
				return Getters.InventoryItem.get(item, function (itemData) {
					ItemStoredTransaction.Transfer.setupForm(itemData, stored, buttonElement);
					Main.processStop();
				});
			}

			if (typeof stored === "string" || (stored instanceof String)) {
				return Getters.StoredItem.getStored(item.id, stored, function (storedData) {
					ItemStoredTransaction.Transfer.setupForm(item, storedData, buttonElement);
					Main.processStop();
				});
			}
			console.log("Setting up stored transfer form for stored item/stored: ", item, stored);
			let promises = [];

			ItemStoredTransaction.Transfer.itemIdInput.val(item.id);
			ItemStoredTransaction.Transfer.itemInfoItemName.text(item.name);
			ItemStoredTransaction.Transfer.itemInfoContainer.show();

			ItemStoredSearchSelect.setupInputs(ItemStoredTransaction.Transfer.fromStoredSelect, item, stored);
			ItemStoredSearchSelect.setupInputs(ItemStoredTransaction.Transfer.toStoredSelect, item);

			let typeSelect = false;
			let fromBlock = false;
			let fromStored = false;
			let amount = false;
			let toBlock = false;
			let toStored = false;
			StorageTypeUtils.runForType(
				item,
				function(){
					fromBlock = true;
					toBlock = true;
					amount = true;
				},
				function (){
					typeSelect = true;
					fromStored = true;
					amount = true;
					toStored = true;
					toBlock = true;
				},
				function (){
					fromStored = true;
					toBlock = true;
				},
				function (){
					fromBlock = true;
					toBlock = true;
				}
			);

			//populate inputs that are used
			if(typeSelect){
				ItemStoredTransaction.Transfer.transactionTypeContainer.show();
			}
			if(fromBlock){
				ItemStoredTransaction.Transfer.fromBlockContainer.show();
				item.storageBlocks.forEach(function (blockId){
					let newBlockOption = $('<option></option>');
					newBlockOption.val(blockId);
					promises.push(getStorageBlockLabel(blockId, function (blockLabel){
						newBlockOption.text(blockLabel);
						//TODO:: select from block if stored given
					}));
					ItemStoredTransaction.Transfer.fromBlockSelect.append(newBlockOption);
				});

			}
			if(fromStored){
				ItemStoredTransaction.Transfer.fromStoredContainer.show();
			}
			if(amount){
				ItemStoredTransaction.Transfer.amountInputContainer.show();

				promises.push(
					StoredFormInput.getAmountInputs(item, stored, true, true).then(function (inputs){
						ItemStoredTransaction.Transfer.amountInputs.html(inputs);
					})
				);
			}
			if(toBlock){
				ItemStoredTransaction.Transfer.toBlockContainer.show();
				item.storageBlocks.forEach(function (blockId){
					let newBlockOption = $('<option></option>');
					newBlockOption.val(blockId);
					promises.push(getStorageBlockLabel(blockId, function (blockLabel){
						newBlockOption.text(blockLabel);
					}));
					ItemStoredTransaction.Transfer.toBlockSelect.append(newBlockOption);
				});
			}
			if(toStored){
				ItemStoredTransaction.Transfer.toStoredContainer.show();
			}

			Promise.all(promises);
			//TODO:: make & run update to block to not select same block as is selected in from

			ItemStoredTransaction.Transfer.updateForm();
			Main.processStop();
		},
		/**
		 * Updates visibility of fields, selected/disabled in dropdowns based on what is selected
		 */
		updateForm(){
			if(ItemStoredTransaction.Transfer.transactionTypeContainer.is(":visible")){
				console.debug("Type input was visible!");

				//TODO:: if amount

				//TODO:: if whole
			}

			//TODO visibility based on type if type is visible
			//TODO:: enabledness of amount if checked and visible
			//TODO:: max amount of amount based on from if amount visible
			//TODO:: enable/disable "to" options based on "from" selections
		},
		updateAllAmount(){
			let inputs = ItemStoredTransaction.Transfer.amountInputs.find("input, select");
			if(ItemStoredTransaction.Transfer.amountTransferAllInput.is(":checked")){
				inputs.prop("disabled", true);
			} else {
				inputs.prop("disabled", false);
			}
		},
		submitFormHandler: async function(event){
			event.preventDefault();
			let transaction = {};

			//TODO:: simple validation: to/from same stored
			//TODO:: fill out transaction

			if(ItemStoredTransaction.Transfer.amountInputContainer.is(":visible")){
				transaction['transactionType'] = "TRANSFER_AMOUNT";
				if(ItemStoredTransaction.Transfer.amountTransferAllInput.is(":checked")){
					transaction['all'] = true;
				} else {
					transaction['amount'] = UnitUtils.getQuantityFromInputs(ItemStoredTransaction.Transfer.amountInputs);
				}
			} else {
				transaction['transactionType'] = "TRANSFER_WHOLE";
			}

			if(ItemStoredTransaction.Transfer.fromBlockContainer.is(":visible")){
				transaction['fromBlock'] = ItemStoredTransaction.Transfer.fromBlockSelect.val();
			}

			if(ItemStoredTransaction.Transfer.fromStoredContainer.is(":visible")){
				transaction['fromStored'] = ItemStoredTransaction.Transfer.fromStoredStoredIdInput.val();
			}

			if(ItemStoredTransaction.Transfer.toBlockContainer.is(":visible")){
				transaction['toBlock'] = ItemStoredTransaction.Transfer.toBlockSelect.val();
			}

			if(ItemStoredTransaction.Transfer.toStoredContainer.is(":visible")){
				transaction['toBlock'] = ItemStoredTransaction.Transfer.toStoredIdInput.val();
			}

			console.log("Built transaction object: ", transaction);
			await ItemStoredTransaction.submitTransaction(
				ItemStoredTransaction.Transfer.itemIdInput.val(),
				transaction,
				ItemStoredTransaction.Transfer.modal
			);
		}
	}
};

ItemStoredTransaction.Add.form.on("submit", ItemStoredTransaction.Add.submitFormHandler);
ItemStoredTransaction.Add.itemIdInput.on("change", function (){
	let itemId = ItemStoredTransaction.Add.itemIdInput.val();
	console.log("Got item for add transaction form. Setting up: ", itemId);
	ItemStoredTransaction.Add.setupForm(itemId);
});

ItemStoredTransaction.Transfer.fromStoredStoredIdInput.on("change", function (){
	let itemId = ItemStoredTransaction.Transfer.fromStoredItemIdInput.val();
	let itemStoredId = ItemStoredTransaction.Transfer.fromStoredStoredIdInput.val();
	console.log("Updating item stored form data ", itemStoredId);

	if(itemId != null && itemStoredId != null){
		Getters.InventoryItem.get(itemId, function(item){
			if(itemStoredId != null){
				Getters.StoredItem.getStored(itemId, itemStoredId, function (stored) {
					StoredFormInput.getAmountInputs(item, stored, true, true).then(function (inputs){
						ItemStoredTransaction.Transfer.amountInputs.html(inputs);
					});
					//TODO:: update stored info?
				});
			} else {
				StoredFormInput.getAmountInputs(item, null, true, true).then(function (inputs){
					ItemStoredTransaction.Transfer.amountInputs.html(inputs);
				});
			}
		});
	}
});
ItemStoredTransaction.Transfer.form.on("submit", ItemStoredTransaction.Transfer.submitFormHandler);
