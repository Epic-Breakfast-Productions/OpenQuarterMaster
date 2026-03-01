import {Rest} from "../../Rest.js";
import {PageMessageUtils} from "../../PageMessageUtils.js";
import {Getters} from "../Getters.js";
import {ModalUtils} from "../../ModalUtils.js";
import {UnitUtils} from "../UnitUtils.js";
import {Links} from "../../links.js";
import {KeywordAttEdit} from "../ObjEditUtils.js";
import {ImageSearchSelect} from "../media/ImageSearchSelect.js";
import {ItemSearchSelect} from "../item/ItemSearchSelect.js";

export const ItemStoredTransaction = {

	// http://localhost:8080/api/passthrough/inventory/item/673c68565986ac44629caf6c/stored/transact
	//                      /api/passthrough/inventory/item/{itemId}                /stored/transact
	submitTransaction: async function (itemId, transaction, transactionModal) {
		console.log("Submitting transaction for item " + itemId + ":", transaction);
		Rest.call({
			spinnerContainer: transactionModal.find(".modal-body")[0],
			failMessagesDiv: transactionModal.find(".messages"),
			method: "PUT",
			url: Rest.passRoot + "/inventory/item/" + itemId + "/stored/transact",
			data: transaction,
			done: function (appliedTransaction) {
				console.log("Successfully applied transaction: ", appliedTransaction);
				PageMessageUtils.reloadPageWithMessage("Transaction Successful!", "success", "Success!");
			}
		});
	},
	ModalButtons: {
		getTransactionSelectDropdown: async function (
			item = null,
			stored = null,
			{
				buttonText = true,
				showAddTransaction = true,
				showSubtractTransaction = true,
				showTransferTransaction = true,
				showCheckinTransaction = true,
				showCheckoutTransaction = false,
				showSetTransaction = true,
			}
		) {

			let query = new URLSearchParams();

			if (item != null) {
				if (typeof item === 'string' || item instanceof String) {
					query.set("item", item);
				} else {
					query.set("item", item.id);
				}
			}
			if (stored != null) {
				if (typeof stored === 'string' || stored instanceof String) {
					query.set("stored", stored);
				} else {
					query.set("stored", stored.id);
				}
			}
			//TODO:: add flags to query

			let output;
			await Rest.call({
				url: Rest.componentRoot + "/itemStoredTransaction/dropdown?" + query.toString(),
				returnType: "html",
				done: function (buttonData) {
					output = $(buttonData);
				}
			});

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
			ModalUtils.setReturnModal(this.modal, buttonElement);
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
				type: this.typeInput.val(),
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

			switch (data.type) {
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

		formContainer: $("#itemStoredTransactionCheckinFormContainer"),

		checkoutIdInput: $("#itemStoredTransactionCheckinFormCheckoutIdInput"),

		checkoutSearchContainer: $("#itemStoredTransactionCheckinCheckoutSearchContainer"),
		checkoutSearchInputGroup: $("#itemStoredTransactionCheckinFormCheckoutSearch-inputGroup"),
		checkoutDetailsContainer: $("#itemStoredTransactionCheckinCheckoutDetailsContainer"),

		byWhoSelect: $("#itemStoredTransactionCheckinFormByWhoSelectInput"),
		byOriginalContainer: $("#itemStoredTransactionCheckinFormByOriginalContainer"),
		byOriginalContent: $("#itemStoredTransactionCheckinFormByOriginalContent"),
		byUserContainer: $("#itemStoredTransactionCheckinFormByUserContainer"),
		byUserInput: $("#itemStoredTransactionCheckinFormByUserInput"),
		byExtContainer: $("#itemStoredTransactionCheckinFormByExtUserContainer"),
		byExtNameInput: $("#itemStoredTransactionCheckinFormByExtUserNameInput"),
		byExtIdInput: $("#itemStoredTransactionCheckinFormByExtUserIdInput"),

		notesInput: $("#itemStoredTransactionCheckinFormNotesInput"),

		toSelectContainer: $("#itemStoredTransactionCheckinFormToSelectContainer"),
		toSelectInput: $("#itemStoredTransactionCheckinFormToSelectInput"),

		toOrigDescContainer: $("#itemStoredTransactionCheckinFormToOriginalDescContainer"),
		toOrigDesc: $("#itemStoredTransactionCheckinFormToOriginalDesc"),

		toBlockContainer: $("#itemStoredTransactionCheckinFormToBlockContainer"),
		toBlockInput: $("#itemStoredTransactionCheckinFormToBlockSelect"),

		toStoredContainer: $("#itemStoredTransactionCheckinFormToStoredContainer"),
		toStoredInputGroup: $("#itemStoredTransactionCheckinFormToItemStored-inputGroup"),
		toStoredIdInput: $("#itemStoredTransactionCheckinFormToItemStored-itemStoredInputId"),

		imageSelect: $("#itemStoredTransactionCheckinForm.imagesSelected"),
		fileSelect: $("#itemStoredTransactionCheckinForm.fileAttachmentSelectInputTable"),
		keywordInputs: $("#itemStoredTransactionCheckinForm.keywordInputDiv"),
		attInputs: $("#itemStoredTransactionCheckinForm.attInputDiv"),

		resetForm() {
			ItemStoredTransaction.Checkin.form.trigger("reset");
			ItemCheckoutSearchSelect.resetSearchInput(ItemStoredTransaction.Checkin.checkoutSearchInputGroup);

			ItemStoredTransaction.Checkin.checkoutIdInput.val("");

			ItemStoredTransaction.Checkin.checkoutSearchContainer.hide();
			ItemStoredTransaction.Checkin.checkoutDetailsContainer.hide();
			ItemStoredTransaction.Checkin.checkoutDetailsContainer.html("");
			ItemStoredTransaction.Checkin.formContainer.hide();


			ItemStoredTransaction.Checkin.byOriginalContainer.hide();
			ItemStoredTransaction.Checkin.byOriginalContent.text("");

			ItemStoredTransaction.Checkin.byUserContainer.hide();
			// ItemStoredTransaction.Checkin.byUserInput

			ItemStoredTransaction.Checkin.byExtContainer.hide();
			ItemStoredTransaction.Checkin.byExtNameInput.val("");
			ItemStoredTransaction.Checkin.byExtIdInput.val("");


			ItemStoredTransaction.Checkin.notesInput.val("");

			ItemStoredTransaction.Checkin.toSelectInput.find("option").prop("disabled", true);

			ItemStoredTransaction.Checkin.toOrigDescContainer.hide();
			ItemStoredTransaction.Checkin.toOrigDesc.text("");

			ItemStoredTransaction.Checkin.toBlockContainer.hide();
			ItemStoredTransaction.Checkin.toBlockInput.html("");

			ItemStoredTransaction.Checkin.toStoredContainer.hide();
			ItemStoredSearchSelect.resetSearchInput(ItemStoredTransaction.Checkin.toStoredInputGroup);

			ItemStoredTransaction.Checkin.imageSelect.text("");
			ItemStoredTransaction.Checkin.keywordInputs.text("");
			ItemStoredTransaction.Checkin.attInputs.text("");

		},
		setupForm: async function (checkout, buttonElement) {
			console.log("Setting up item stored checkin transaction form for checkin ", checkout);
			ModalUtils.setReturnModal(this.modal, buttonElement);
			this.resetForm();

			if (checkout == null) {
				console.log("No checkout given. Enabling search.");
				ItemStoredTransaction.Checkin.checkoutSearchContainer.show();
				return;
			}

			if (typeof checkout === "string" || (checkout instanceof String)) {
				checkout = await Getters.Checkout.getCheckout(checkout);
			}

			if (!checkout.stillCheckedOut) {
				PageMessageUtils.addMessageToDiv(ItemStoredTransaction.Checkin.messages, "danger", "This has already been checked in.");
			}

			let item = null;
			await Getters.InventoryItem.get(checkout.item, function (itemData) {
				item = itemData;
			});

			console.log("Setting up item checkin form for: ", checkout, item);

			ItemStoredTransaction.Checkin.formContainer.show();
			ItemStoredTransaction.Checkin.checkoutIdInput.val(checkout.id);

			let itemCheckoutDisplay = ItemCheckoutView.getCheckoutDisplay(checkout);


			if (ItemStoredTransaction.Checkin.byUserInput.empty()) {
				Getters.InteractingEntities.getEntities({
					type: "USER",
					doneFunc: function (users) {
						users.forEach(function (user) {
							let newOp = $('<option></option>');
							newOp.text(user.username + " / " + user.name);
							newOp.val(user.id);
							ItemStoredTransaction.Checkin.byUserInput.append(newOp);
						});
					}
				});
			}

			//Setup Storage blocks in select
			item.storageBlocks.forEach(function (blockId) {
				let newBlockOption = $('<option></option>');
				newBlockOption.val(blockId);
				Getters.StorageBlock.getStorageBlockLabel(blockId, function (blockLabel) {
					newBlockOption.text(blockLabel);
				});
				ItemStoredTransaction.Checkin.toBlockInput.append(newBlockOption);
			});

			//Enable options based on checkout/item
			let toBlock = function () {
				ItemStoredTransaction.Checkin.toSelectInput.find("option[value='block']").prop("disabled", false);
			}

			CheckoutTypeUtils.runForType(
				checkout,
				function () {
					StorageTypeUtils.runForType(
						item,
						toBlock,
						function () {
							ItemStoredTransaction.Checkin.toSelectInput.find("option[value='block']").prop("disabled", false);
							ItemStoredTransaction.Checkin.toSelectInput.find("option[value='stored']").prop("disabled", false);
							ItemStoredSearchSelect.setupInputs(ItemStoredTransaction.Checkin.toStoredInputGroup, item);
						},
						toBlock,
						toBlock
					);
				},
				function () {
					toBlock();
				}
			);


			let originalOption = ItemStoredTransaction.Checkin.toSelectInput.find("option[value='original']");
			let blockOriginal = function () {
				if (item.storageBlocks.includes(checkout.fromBlock)) {
					originalOption.prop("disabled", false);
					Getters.StorageBlock.getStorageBlockLabel(checkout.fromBlock, function (blockLabel) {
						ItemStoredTransaction.Checkin.toOrigDesc.html(
							Links.getStorageViewLink(checkout.fromBlock, blockLabel)
						);
					});
				}
			}

			await CheckoutTypeUtils.runForType(
				checkout,
				async function () {
					await StorageTypeUtils.runForType(
						item,
						blockOriginal,
						async function () {
							try {
								await Getters.StoredItem.getStored(item.id, checkout.fromStored, function (storedItem) {
									originalOption.prop("disabled", false);
									ItemStoredTransaction.Checkin.toOrigDesc.text(
										storedItem.labelText
									);
									//TODO:: more stored details (block)
								});
							} catch (e) {
								console.log("Original stored no longer present.")
							}
						},
						blockOriginal,
						blockOriginal
					);
				},
				blockOriginal
			);

			ItemStoredTransaction.Checkin.toSelectInput.find("option:not(:disabled)").first().prop("selected", true);

			ItemStoredTransaction.Checkin.checkoutDetailsContainer.append(await itemCheckoutDisplay);
			ItemStoredTransaction.Checkin.updateCheckedInBy();
			ItemStoredTransaction.Checkin.intoChanged();
			ItemStoredTransaction.Checkin.checkoutDetailsContainer.show();
		},
		updateCheckedInBy() {
			let checkedInBySelectVal = ItemStoredTransaction.Checkin.byWhoSelect.val();

			ItemStoredTransaction.Checkin.byOriginalContainer.hide();
			ItemStoredTransaction.Checkin.byUserContainer.hide();
			ItemStoredTransaction.Checkin.byExtContainer.hide();

			switch (checkedInBySelectVal) {
				case "originalFor":
					ItemStoredTransaction.Checkin.byOriginalContainer.show();
					ItemStoredTransaction.Checkin.byOriginalContent.html(
						ItemStoredTransaction.Checkin.checkoutDetailsContainer.find(".itemCheckoutViewCheckedOutForLabel").clone()
					);
					break;
				case "originalBy":
					ItemStoredTransaction.Checkin.byOriginalContainer.show();
					ItemStoredTransaction.Checkin.byOriginalContent.html(
						ItemStoredTransaction.Checkin.checkoutDetailsContainer.find(".itemCheckoutViewCheckedOutByLabel").clone()
					);
					break;
				case "self":
					break;
				case "otherOqmUser":
					ItemStoredTransaction.Checkin.byUserContainer.show();
					break;
				case "extUser":
					ItemStoredTransaction.Checkin.byExtContainer.show();
					break;
			}
		},
		intoChanged() {
			ItemStoredTransaction.Checkin.toOrigDescContainer.hide();
			ItemStoredTransaction.Checkin.toBlockContainer.hide();
			ItemStoredTransaction.Checkin.toStoredContainer.hide();

			switch (ItemStoredTransaction.Checkin.toSelectInput.val()) {
				case "original":
					ItemStoredTransaction.Checkin.toOrigDescContainer.show();
					break;
				case "block":
					ItemStoredTransaction.Checkin.toBlockContainer.show();
					break;
				case "stored":
					ItemStoredTransaction.Checkin.toStoredContainer.show();
					break;
			}
		},
		submitFormHandler: async function (e) {
			e.preventDefault();
			console.log("Submitting Checkin form");

			await Getters.Checkout.getCheckout(ItemStoredTransaction.Checkin.checkoutIdInput.val(), async function (checkoutData) {
				let item = Getters.InventoryItem.get(checkoutData.item);
				let transactionData = {
					type: "CHECKIN_FULL",
					checkoutId: checkoutData.id,
					details: {
						type: "RETURN_FULL",
						notes: ItemStoredTransaction.Checkin.notesInput.val(),
						imageIds: [],
						attachedFiles: FileAttachmentSearchSelect.getFileListFromInput(
							ItemStoredTransaction.Checkin.fileSelect
						),
						keywords: [],
						attributes: {}
					}
				};
				KeywordAttEdit.addKeywordAttData(
					transactionData.details,
					ItemStoredTransaction.Checkin.keywordInputs,
					ItemStoredTransaction.Checkin.attInputs
				);
				ImageSearchSelect.addImagesToData(transactionData.details, ItemStoredTransaction.Checkin.imageSelect);

				switch (ItemStoredTransaction.Checkin.byWhoSelect.val()) {
					case "originalFor":
						let originalFor = checkoutData.checkoutDetails.checkedOutFor;

						switch (originalFor.type) {
							case "OQM_USER":
								transactionData.details.checkedInBy = {
									type: "OQM_USER",
									entity: originalFor.entity
								}
								break;
							case "EXT_SYS_USER":
								transactionData.details.checkedInBy = {
									type: "EXT_SYS_USER",
									externalId: originalFor.externalId,
									name: originalFor.name
								}
								break;
						}
						break;
					case "originalBy":
						transactionData.details.checkedInBy = {
							type: "OQM_USER",
							entity: checkoutData.checkedOutByEntity
						}
						break;
					case "self":
						transactionData.details.checkedInBy = {
							type: "OQM_USER",
							entity: UserUtils.userId
						}
						break;
					case "otherOqmUser":
						transactionData.details.checkedInBy = {
							type: "OQM_USER",
							entity: ItemStoredTransaction.Checkin.byUserInput.val()
						}
						break;
					case "extUser":
						transactionData.details.checkedInBy = {
							type: "EXT_SYS_USER",
							externalId: ItemStoredTransaction.Checkin.byExtIdInput.val(),
							name: ItemStoredTransaction.Checkin.byExtNameInput.val()
						}
						break;
				}

				switch (ItemStoredTransaction.Checkin.toSelectInput.val()) {
					case "original":
						let blockOriginal = function () {
							transactionData.toBlock = checkoutData.fromBlock;
						};
						await CheckoutTypeUtils.runForType(
							checkoutData,
							async function () {
								StorageTypeUtils.runForType(
									await item,
									blockOriginal,
									async function () {
										transactionData.toStored = checkoutData.fromStored;
									},
									blockOriginal,
									blockOriginal
								);
							},
							blockOriginal
						);
						break;
					case "block":
						transactionData.toBlock = ItemStoredTransaction.Checkin.toBlockInput.val();
						break;
					case "stored":
						transactionData.toStored = ItemStoredTransaction.Checkin.toStoredIdInput.val();
						break;
				}

				console.log("Submitting checkin transaction: ", transactionData);
				await ItemStoredTransaction.submitTransaction(
					checkoutData.item,
					transactionData,
					ItemStoredTransaction.Checkin.modal
				);
			});
		}
	},
	Checkout: {
		modal: $("#itemStoredTransactionCheckoutModal"),
		messages: $("#itemStoredTransactionCheckoutMessages"),
		form: $("#itemStoredTransactionCheckoutForm"),
		formContainer: $("#itemStoredTransactionCheckoutFormContainer"),

		itemSearchContainer: $("#itemStoredTransactionCheckoutFormItemInputContainer"),
		itemSearchIdInput: $("#itemStoredTransactionCheckoutFormItem-itemInputId"),
		itemSearchClearButton: $("#itemStoredTransactionCheckoutFormItem-itemInputClearButton"),

		itemIdInput: $("#itemStoredTransactionCheckoutFormItemIdInput"),

		itemInfoContainer: $("#itemStoredTransactionCheckoutFormItemInfoContainer"),
		itemInfoName: $("#itemStoredTransactionCheckoutFormInfoItemName"),

		typeInputContainer: $("#itemStoredTransactionCheckoutFormTypeInputContainer"),
		typeInput: $("#itemStoredTransactionCheckoutFormTypeInput"),

		fromBlockContainer: $("#itemStoredTransactionCheckoutFormFromBlockContainer"),
		fromBlockSelect: $("#itemStoredTransactionCheckoutFormFromBlockSelect"),

		fromStoredContainer: $("#itemStoredTransactionCheckoutFormFromStoredContainer"),
		fromStoredInputGroup: $("#itemStoredTransactionCheckoutFormFromItemStored-inputGroup"),
		fromStoredBlockIdInput: $("#itemStoredTransactionCheckoutFormFromItemStored-itemStoredInputBlockId"),
		fromStoredStoredIdInput: $("#itemStoredTransactionCheckoutFormFromItemStored-itemStoredInputId"),

		amountContainer: $("#itemStoredTransactionCheckoutFormAmountContainer"),
		amountInputs: $("#itemStoredTransactionCheckoutFormAmountInputs"),
		amountAllInput: $("#itemStoredTransactionCheckoutFormAmountAllInput"),

		forWhomSelect: $("#itemStoredTransactionCheckoutFormForSelectInput"),

		forUserContainer: $("#itemStoredTransactionCheckoutFormForUserContainer"),
		forUserSelect: $("#itemStoredTransactionCheckoutFormForUserInput"),

		forExternalContainer: $("#itemStoredTransactionCheckoutFormForExtUserContainer"),
		forExternalNameInput: $("#itemStoredTransactionCheckoutFormForExtUserNameInput"),
		forExternalIdInput: $("#itemStoredTransactionCheckoutFormForExtUserIdInput"),

		dueBackInput: $("#itemStoredTransactionCheckoutFormDueBackInput"),
		reasonInput: $("#itemStoredTransactionCheckoutFormReasonInput"),
		detailsInput: $("#itemStoredTransactionCheckoutFormDetailDetailInput"),

		resetForm() {
			ItemStoredTransaction.Checkout.form.trigger("reset");
			ItemStoredTransaction.Checkout.formContainer.hide();
			ItemStoredTransaction.Checkout.messages.text("");

			ItemStoredTransaction.Checkout.itemSearchContainer.hide();
			ItemSearchSelect.clearSearchInput(ItemStoredTransaction.Checkout.itemSearchClearButton, false);

			ItemStoredTransaction.Checkout.itemInfoContainer.hide();
			ItemStoredTransaction.Checkout.itemInfoName.text("");

			ItemStoredTransaction.Checkout.itemIdInput.val("");

			ItemStoredTransaction.Checkout.typeInputContainer.hide();

			ItemStoredTransaction.Checkout.fromBlockContainer.hide();
			ItemStoredTransaction.Checkout.fromBlockSelect.text("");

			ItemStoredTransaction.Checkout.fromStoredContainer.hide();
			ItemStoredSearchSelect.resetSearchInput(ItemStoredTransaction.Checkout.fromStoredInputGroup);

			ItemStoredTransaction.Checkout.amountContainer.hide();
			ItemStoredTransaction.Checkout.amountInputs.text("");

			ItemStoredTransaction.Checkout.forUserContainer.hide();
			ItemStoredTransaction.Checkout.forUserSelect.text("");


			ItemStoredTransaction.Checkout.forExternalContainer.hide();
		},
		setupForm: async function (item, stored, buttonElement) {
			Main.processStart();
			ItemStoredTransaction.Checkout.resetForm();
			if (buttonElement != null) {
				ModalUtils.setReturnModal(ItemStoredTransaction.Checkout.modal, buttonElement);
			}
			if (item == null && stored == null) {
				console.log("No item given.")
				ItemStoredTransaction.Checkout.itemSearchContainer.show();
				Main.processStop();
				return;
			}

			if (typeof item === "string" || (item instanceof String)) {
				console.log("Got item id")
				return Getters.InventoryItem.get(item, function (itemData) {
					ItemStoredTransaction.Checkout.setupForm(itemData, stored, buttonElement);
					Main.processStop();
				});
			}

			if (typeof stored === "string" || (stored instanceof String)) {
				console.log("Got stored id")
				return Getters.StoredItem.getStored(item.id, stored, function (storedData) {
					ItemStoredTransaction.Checkout.setupForm(item, storedData, buttonElement);
					Main.processStop();
				});
			}
			console.log("Setting up stored checkout form for stored item/stored: ", item, stored);
			let promises = [];

			ItemStoredTransaction.Checkout.formContainer.show();

			ItemStoredTransaction.Checkout.itemIdInput.val(item.id);
			ItemStoredTransaction.Checkout.itemInfoName.text(item.name);
			ItemStoredTransaction.Checkout.itemInfoContainer.show();

			if (ItemStoredTransaction.Checkout.forUserSelect.empty()) {
				Getters.InteractingEntities.getEntities({
					type: "USER",
					doneFunc: function (users) {
						users.forEach(function (user) {
							let newOp = $('<option></option>');
							newOp.text(user.username + " / " + user.name);
							newOp.val(user.id);
							ItemStoredTransaction.Checkout.forUserSelect.append(newOp);
						});
					}
				});
			}

			promises.push(ItemStoredSearchSelect.setupInputs(ItemStoredTransaction.Checkout.fromStoredInputGroup, item, stored));

			let typeSelect = false;
			let fromBlock = false;
			let fromStored = false;
			let amount = false;

			StorageTypeUtils.runForType(
				item,
				function () {
					fromBlock = true;
					amount = true;
				},
				function () {
					typeSelect = true;
					fromStored = true;
					amount = true;
				},
				function () {
					fromStored = true;
				},
				function () {
					fromStored = true;
				}
			);

			if (typeSelect) {
				console.debug("Showing type select");
				ItemStoredTransaction.Checkout.typeInputContainer.show();

			}
			if (fromBlock) {
				console.debug("Showing from block");
				ItemStoredTransaction.Checkout.fromBlockContainer.show();
				item.storageBlocks.forEach(function (blockId) {
					let newBlockOption = $('<option></option>');
					newBlockOption.val(blockId);
					promises.push(Getters.StorageBlock.getStorageBlockLabel(blockId, function (blockLabel) {
						newBlockOption.text(blockLabel);
					}));
					ItemStoredTransaction.Checkout.fromBlockSelect.append(newBlockOption);
				});
			}
			if (fromStored) {
				console.debug("Showing from stored");
				ItemStoredTransaction.Checkout.fromStoredContainer.show();
				promises.push(ItemStoredSearchSelect.setupInputs(
					ItemStoredTransaction.Checkout.fromStoredInputGroup,
					item,
					stored
				));
			}
			if (amount) {
				console.debug("Showing amount inputs");
				ItemStoredTransaction.Checkout.amountContainer.show();
			}

			await Promise.all(promises);

			ItemStoredTransaction.Checkout.updateForWho();

			if (typeSelect) {
				ItemStoredTransaction.Checkout.typeUpdated(true);
			}
			if (amount) {
				ItemStoredTransaction.Checkout.updateAmount(item, stored, null, true);
			}

			Main.processStop();
		},
		typeUpdated(force = false) {
			if (force || ItemStoredTransaction.Checkout.typeInput.is(":visible")) {
				console.log("Updating inputs based on selected subtract type.");

				let newType = ItemStoredTransaction.Checkout.typeInput.val();
				switch (newType) {
					case("SUBTRACT_WHOLE"):
						ItemStoredTransaction.Checkout.amountContainer.hide();
						// ItemStoredTransaction.Subtract.fromStoredContainer.show();
						// ItemStoredTransaction.Subtract.fromBlockContainer.hide();
						break;
					case("SUBTRACT_AMOUNT"):
						ItemStoredTransaction.Checkout.amountContainer.show();
						// ItemStoredTransaction.Subtract.fromStoredContainer.hide();
						// ItemStoredTransaction.Subtract.fromBlockContainer.show();
						ItemStoredTransaction.Checkout.updateAmount(null, null, null, true);
						break;
				}
			}
		},
		updateAmount: async function (item = null, stored = null, storageBlockId = null, force = false) {
			if (force || ItemStoredTransaction.Checkout.amountInputsContainer.is(":visible")) {
				console.log("Updating amounts");

				if (item == null) {
					item = ItemStoredTransaction.Checkout.itemIdInput.val();
					console.debug("Got item id from form: ", item)
				}
				if (typeof item === 'string' || item instanceof String) {
					Getters.InventoryItem.get(item, function (itemData) {
						ItemStoredTransaction.Checkout.updateAmount(itemData, stored, storageBlockId, force);
					});
					return;
				}

				if (stored != null) {
					console.log("Stored specified: ", stored)
					if (stored === -1) {
						stored = null;
					} else {
						if (typeof stored === 'string' || stored instanceof String) {
							await Getters.StoredItem.getStored(item.id, stored, function (storedData) {
								stored = storedData;
							});
						}
					}
					console.log("final stored for amount input generation: ", stored)
					StoredFormInput.getAmountInputs(item, stored, true, true).then(function (inputs) {
						ItemStoredTransaction.Checkout.amountInputs.html(inputs);
						ItemStoredTransaction.Checkout.updateAllAmount();
					});
				} else {//find stored
					console.log("Stored not specified. Gleaning from form.");
					if (storageBlockId == null && ItemStoredTransaction.Checkout.fromBlockSelect.is(":visible")) {
						storageBlockId = ItemStoredTransaction.Checkout.fromBlockSelect.val();
						console.debug("Got storage block id from block select: ", storageBlockId);
					}
					if (storageBlockId == null && ItemStoredTransaction.Checkout.fromStoredBlockIdInput.is(":visible")) {
						storageBlockId = ItemStoredTransaction.Checkout.fromStoredBlockIdInput.val();
						console.debug("Got storage block id from stored select: ", storageBlockId);
					}
					if (stored == null && ItemStoredTransaction.Checkout.fromStoredStoredIdInput.is(":visible")) {
						stored = ItemStoredTransaction.Checkout.fromStoredStoredIdInput.val();
						console.debug("Got stored id from form: ", stored);
					}

					console.log("Stored/ Block: ", stored, storageBlockId);

					if (!stored && !storageBlockId) {
						console.log("No stored or storage block id could be identified.");
						StoredFormInput.getAmountInputs(item, null, true, true).then(function (inputs) {
							ItemStoredTransaction.Checkout.amountInputs.html(inputs);
							ItemStoredTransaction.Checkout.updateAllAmount();
						});
						return;
					}

					if (stored != null) {
						ItemStoredTransaction.Checkout.updateAmount(item, stored, storageBlockId, force);
						return;
					}
					if (storageBlockId != null) {
						Getters.StoredItem.getSingleStoredForItemInBlock(item.id, storageBlockId, function (storedData) {
								ItemStoredTransaction.Checkout.updateAmount(item, storedData, storageBlockId, force);
							},
							function () {
								ItemStoredTransaction.Checkout.updateAmount(item, -1, storageBlockId, force)
							}
						);
						return;
					}
					throw new Error("Should not be able to get here.");
				}
			} else {
				console.debug("Amounts not visible; not updating.");
			}
		},
		updateAllAmount() {
			let inputs = ItemStoredTransaction.Checkout.amountInputs.find("input, select");
			if (ItemStoredTransaction.Checkout.amountAllInput.is(":checked")) {
				inputs.prop("disabled", true);
			} else {
				inputs.prop("disabled", false);
			}
		},
		updateForWho() {
			ItemStoredTransaction.Checkout.forExternalContainer.hide();
			ItemStoredTransaction.Checkout.forUserContainer.hide();
			let who = ItemStoredTransaction.Checkout.forWhomSelect.val();

			switch (who) {
				case "otherOqmUser":
					ItemStoredTransaction.Checkout.forUserContainer.show();
					break;
				case "extUser":
					ItemStoredTransaction.Checkout.forExternalContainer.show();
					break;
			}
		},
		submitFormHandler: async function (event) {
			event.preventDefault();
			console.log("Submitting Item Stored Checkout form.");

			let transaction = {
				checkoutDetails: {
					checkedOutFor: {},
					dueBack: ItemStoredTransaction.Checkout.dueBackInput.val(),
					reason: ItemStoredTransaction.Checkout.reasonInput.val(),
					notes: ItemStoredTransaction.Checkout.detailsInput.val()
				}
			};
			switch (ItemStoredTransaction.Checkout.forWhomSelect.val()) {
				case "self":
					transaction.checkoutDetails.checkedOutFor["type"] = "OQM_ENTITY";
					transaction.checkoutDetails.checkedOutFor["entity"] = UserUtils.userId;
					break;
				case "otherOqmUser":
					transaction.checkoutDetails.checkedOutFor["type"] = "OQM_ENTITY";
					transaction.checkoutDetails.checkedOutFor["entity"] = ItemStoredTransaction.Checkout.forUserSelect.val();
					break;
				case "extUser":
					transaction.checkoutDetails.checkedOutFor["type"] = "EXT_SYS_USER";
					transaction.checkoutDetails.checkedOutFor["externalId"] = ItemStoredTransaction.Checkout.forExternalIdInput.val();
					transaction.checkoutDetails.checkedOutFor["name"] = ItemStoredTransaction.Checkout.forExternalNameInput.val();
					break;
			}

			if (ItemStoredTransaction.Checkout.amountInputs.is(":visible")) {
				transaction['type'] = "CHECKOUT_AMOUNT";
				if (ItemStoredTransaction.Checkout.amountAllInput.is(":checked")) {
					transaction['all'] = true;
				} else {
					transaction['amount'] = UnitUtils.getQuantityFromInputs(ItemStoredTransaction.Checkout.amountInputs);
				}
			} else {
				transaction['type'] = "CHECKOUT_WHOLE";
			}

			if (ItemStoredTransaction.Checkout.fromBlockContainer.is(":visible")) {
				transaction['fromBlock'] = ItemStoredTransaction.Checkout.fromBlockSelect.val();
			}

			if (ItemStoredTransaction.Checkout.fromStoredContainer.is(":visible")) {
				transaction[
					transaction['type'] === "CHECKOUT_AMOUNT" ? 'fromStored' : 'toCheckout'
					] = ItemStoredTransaction.Checkout.fromStoredStoredIdInput.val();
			}

			console.log("Built checkout transaction object: ", transaction);
			await ItemStoredTransaction.submitTransaction(
				ItemStoredTransaction.Checkout.itemIdInput.val(),
				transaction,
				ItemStoredTransaction.Checkout.modal
			);
		}
	},
	Set: {
		modal: $("#itemStoredTransactionSetModal"),
		messages: $("#itemStoredTransactionSetMessages"),
		form: $("#itemStoredTransactionSetForm"),

		itemSelectContainer: $("#itemStoredTransactionSetFormItemInputContainer"),
		itemIdInput: $("#itemStoredTransactionSetFormItemIdInput"),
		itemName: $("#itemStoredTransactionSetItemInfoItemName"),
		setTypeInput: $("#itemStoredTransactionSetFormItemSetType"),

		blockContainer: $("#itemStoredTransactionSetFormBlockContainer"),
		blockSelect: $("#itemStoredTransactionSetFormBlockSelect"),

		storedContainer: $("#itemStoredTransactionSetFormStoredContainer"),
		storedItemIdInput: $("#itemStoredTransactionSetFormItemStored-itemStoredInputItemId"),
		storedIdInput: $("#itemStoredTransactionSetFormItemStored-itemStoredInputId"),
		storedItemBlockIdInput: $("#itemStoredTransactionSetFormItemStored-itemStoredInputBlockId"),
		storedSelect: $("#itemStoredTransactionSetFormItemStored-inputGroup"),

		amountInputs: $("#itemStoredTransactionSetFormAmountContainer"),

		failNotAmountType() {
			PageMessageUtils.addMessageToDiv(
				ItemStoredTransaction.Set.messages,
				"danger",
				"Cannot set the amount of an item with no amount.",
				"Invalid Item Type"
			);
			Main.processStop();
		},
		resetForm() {
			ItemStoredTransaction.Set.messages.text("");
			ItemStoredTransaction.Set.itemSelectContainer.hide();
			ItemStoredTransaction.Set.itemIdInput.val("");
			ItemStoredTransaction.Set.itemName.text("");
			ItemStoredTransaction.Set.setTypeInput.val("");

			ItemStoredTransaction.Set.blockContainer.hide();
			ItemStoredTransaction.Set.blockSelect.text("");

			ItemStoredTransaction.Set.storedContainer.hide();

			ItemStoredTransaction.Set.amountInputs.text("");
			ItemSearchSelect.clearSearchInput(ItemStoredTransaction.Set.storedSelect, false);

		},
		setupForm: async function (item, stored, buttonElement) {
			Main.processStart();
			if (buttonElement != null) {
				ModalUtils.setReturnModal(this.modal, buttonElement);
			}
			this.resetForm();

			if (item == null && stored == null) {
				console.log("No item given.");
				ItemStoredTransaction.Set.itemSelectContainer.show();
				Main.processStop();
				return;
			}

			if (typeof item === "string" || (item instanceof String)) {
				console.log("Got item id")
				return Getters.InventoryItem.get(item, function (itemData) {
					ItemStoredTransaction.Set.setupForm(itemData, stored, buttonElement);
					Main.processStop();
				});
			}

			if (typeof stored === "string" || (stored instanceof String)) {
				console.log("Got stored id")
				return Getters.StoredItem.getStored(item.id, stored, function (storedData) {
					ItemStoredTransaction.Set.setupForm(item, storedData, buttonElement);
					Main.processStop();
				});
			}
			console.log("Setting up stored set form for stored item/stored: ", item, stored);

			ItemStoredTransaction.Set.itemName.text(item.name);
			ItemStoredTransaction.Set.itemIdInput.val(item.id);

			let promises = [];

			StorageTypeUtils.runForType(
				item,
				function () {
					ItemStoredTransaction.Set.setTypeInput.val("block");
					ItemStoredTransaction.Set.blockContainer.show();
					item.storageBlocks.forEach(function (blockId) {
						let newBlockOption = $('<option></option>');
						newBlockOption.val(blockId);
						if (stored && stored.storageBlock === blockId) {
							newBlockOption.attr("selected", true);
						}
						promises.push(Getters.StorageBlock.getStorageBlockLabel(blockId, function (blockLabel) {
							newBlockOption.text(blockLabel);
						}));
						ItemStoredTransaction.Set.blockSelect.append(newBlockOption);
					});
				},
				function () {
					ItemStoredTransaction.Set.setTypeInput.val("stored");
					promises.push(ItemStoredSearchSelect.setupInputs(ItemStoredTransaction.Set.storedSelect, item, stored));
					ItemStoredTransaction.Set.storedContainer.show();
				},
				ItemStoredTransaction.Set.failNotAmountType,
				ItemStoredTransaction.Set.failNotAmountType
			);
			if (promises) {
				await Promise.all(promises);
			}

			ItemStoredTransaction.Set.updateAmounts();

			Main.processStop();
		},
		updateAmounts: async function (item = null, stored = null, storageBlockId = null) {
			console.log("Updating amounts");

			if (item == null) {
				item = ItemStoredTransaction.Set.itemIdInput.val();
			}
			if (typeof item === 'string' || item instanceof String) {
				Getters.InventoryItem.get(item, function (itemData) {
					ItemStoredTransaction.Set.updateAmounts(itemData, stored, storageBlockId);
				});
				return;
			}

			if (stored != null) {
				console.log("Stored specified: ", stored)
				if (stored === -1) {
					stored = null;
				} else {
					if (typeof stored === 'string' || stored instanceof String) {
						await Getters.StoredItem.getStored(item.id, stored, function (storedData) {
							stored = storedData;
						});
					}
				}
				console.log("final stored for amount input generation: ", stored)
				StoredFormInput.getAmountInputs(item, stored, true, false).then(function (inputs) {
					ItemStoredTransaction.Set.amountInputs.html(inputs);
				});
			} else {//find stored
				console.log("Stored not specified. Gleaning from form.");
				let type = ItemStoredTransaction.Set.setTypeInput.val();

				if (storageBlockId == null && type === "block") {
					storageBlockId = ItemStoredTransaction.Set.blockSelect.val();
					console.debug("Got storage block id from block select: ", storageBlockId);
				}
				if (storageBlockId == null && type === "stored") {
					storageBlockId = ItemStoredTransaction.Set.storedItemBlockIdInput.val();
					console.debug("Got storage block id from stored select: ", storageBlockId);
				}
				if (stored == null && type === "stored") {
					stored = ItemStoredTransaction.Set.storedIdInput.val();
					console.debug("Got stored id from form: ", stored);
				}

				console.log("Stored/ Block: ", stored, storageBlockId);

				if (!stored && !storageBlockId) {
					console.log("No stored or storage block id could be identified.");
					return;
				}

				if (stored != null) {
					ItemStoredTransaction.Set.updateAmounts(item, stored, storageBlockId);
					return;
				}
				if (storageBlockId != null) {
					Getters.StoredItem.getSingleStoredForItemInBlock(item.id, storageBlockId, function (storedData) {
							ItemStoredTransaction.Set.updateAmounts(item, storedData, storageBlockId);
						},
						function () {
							ItemStoredTransaction.Set.updateAmounts(item, -1, storageBlockId)
						}
					);
					return;
				}
				throw new Error("Should not be able to get here.");
			}
		},
		submitFormHandler: async function (event) {
			event.preventDefault();
			let transaction = {
				type: "SET_AMOUNT",
				amount: UnitUtils.getQuantityFromInputs(ItemStoredTransaction.Set.amountInputs)
			};

			let type = ItemStoredTransaction.Set.setTypeInput.val();

			switch (type) {
				case "block":
					transaction["block"] = ItemStoredTransaction.Set.blockSelect.val();
					break;
				case "stored":
					transaction["stored"] = ItemStoredTransaction.Set.storedIdInput.val();
					break;
			}


			console.log("Built Set transaction object: ", transaction);
			await ItemStoredTransaction.submitTransaction(
				ItemStoredTransaction.Set.itemIdInput.val(),
				transaction,
				ItemStoredTransaction.Set.modal
			);
		}
	},
	Subtract: {
		modal: $("#itemStoredTransactionSubtractModal"),
		messages: $("#itemStoredTransactionSubtractMessages"),
		form: $("#itemStoredTransactionSubtractForm"),

		itemSearchContainer: $("#itemStoredTransactionSubtractFormItemInputContainer"),
		itemSearchIdInput: $("#itemStoredTransactionSubtractFormItem-itemInputId"),

		itemInfoContainer: $("#itemStoredTransactionSubtractItemInfoContainer"),
		itemInfoName: $("#itemStoredTransactionSubtractItemInfoItemName"),

		itemIdInput: $("#itemStoredTransactionSubtractFormItemIdInput"),

		typeInputContainer: $("#itemStoredTransactionSubtractFormTypeInputContainer"),
		typeSelect: $("#itemStoredTransactionSubtractFormTypeInput"),

		fromBlockContainer: $("#itemStoredTransactionSubtractFormFromBlockContainer"),
		fromBlockSelect: $("#itemStoredTransactionSubtractFormFromBlockSelect"),

		fromStoredContainer: $("#itemStoredTransactionSubtractFormFromStoredContainer"),
		fromStoredInputGroup: $("#itemStoredTransactionSubtractFormFromItemStored-inputGroup"),
		fromStoredItemIdInput: $("#itemStoredTransactionSubtractFormFromItemStored-itemStoredInputItemId"),
		fromStoredBlockIdInput: $("#itemStoredTransactionSubtractFormFromItemStored-itemStoredInputBlockId"),
		fromStoredStoredIdInput: $("#itemStoredTransactionSubtractFormFromItemStored-itemStoredInputId"),

		amountContainer: $("#itemStoredTransactionSubtractFormAmountContainer"),
		amountInputsContainer: $("#itemStoredTransactionSubtractFormAmountInputs"),
		amountSubtractAllInput: $("#itemStoredTransactionSubtractFormAmountSubtractAllInput"),

		resetForm() {
			ItemStoredTransaction.Subtract.form.trigger("reset");
			ItemStoredTransaction.Subtract.messages.text("");

			ItemStoredTransaction.Subtract.itemSearchContainer.hide();
			ItemSearchSelect.clearSearchInput(ItemStoredTransaction.Subtract.itemSearchContainer, false);

			ItemStoredTransaction.Subtract.itemInfoContainer.hide();
			ItemStoredTransaction.Subtract.itemInfoName.text("");

			ItemStoredTransaction.Subtract.itemIdInput.val("");

			ItemStoredTransaction.Subtract.typeInputContainer.hide();

			ItemStoredTransaction.Subtract.fromBlockContainer.hide();
			ItemStoredTransaction.Subtract.fromBlockSelect.text("");

			ItemStoredTransaction.Subtract.fromStoredContainer.hide();
			ItemStoredSearchSelect.resetSearchInput(ItemStoredTransaction.Subtract.fromStoredInputGroup);

			ItemStoredTransaction.Subtract.amountContainer.hide();
			ItemStoredTransaction.Subtract.amountInputsContainer.text("");
		},
		setupForm: async function (item, stored, buttonElement) {
			Main.processStart();
			if (buttonElement != null) {
				ModalUtils.setReturnModal(this.modal, buttonElement);
			}
			ItemStoredTransaction.Subtract.resetForm();
			if (item == null && stored == null) {
				console.log("No item given.")
				ItemStoredTransaction.Subtract.itemSearchContainer.show();
				Main.processStop();
				return;
			}

			if (typeof item === "string" || (item instanceof String)) {
				console.log("Got item id")
				return Getters.InventoryItem.get(item, function (itemData) {
					ItemStoredTransaction.Subtract.setupForm(itemData, stored, buttonElement);
					Main.processStop();
				});
			}

			if (typeof stored === "string" || (stored instanceof String)) {
				console.log("Got stored id")
				return Getters.StoredItem.getStored(item.id, stored, function (storedData) {
					ItemStoredTransaction.Subtract.setupForm(item, storedData, buttonElement);
					Main.processStop();
				});
			}
			console.log("Setting up stored subtract form for stored item/stored: ", item, stored);
			let promises = [];

			ItemStoredTransaction.Subtract.itemIdInput.val(item.id);
			ItemStoredTransaction.Subtract.itemInfoName.text(item.name);
			ItemStoredTransaction.Subtract.itemInfoContainer.show();

			promises.push(ItemStoredSearchSelect.setupInputs(ItemStoredTransaction.Subtract.fromStoredInputGroup, item, stored));

			let typeSelect = false;
			let fromBlock = false;
			let fromStored = false;
			let amount = false;

			StorageTypeUtils.runForType(
				item,
				function () {
					fromBlock = true;
					amount = true;
				},
				function () {
					typeSelect = true;
					fromStored = true;
					amount = true;
				},
				function () {
					fromStored = true;
				},
				function () {
					fromStored = true;
				}
			);

			if (typeSelect) {
				console.debug("Showing type select");
				ItemStoredTransaction.Subtract.typeInputContainer.show();

			}
			if (fromBlock) {
				console.debug("Showing from block");
				ItemStoredTransaction.Subtract.fromBlockContainer.show();
				item.storageBlocks.forEach(function (blockId) {
					let newBlockOption = $('<option></option>');
					newBlockOption.val(blockId);
					promises.push(Getters.StorageBlock.getStorageBlockLabel(blockId, function (blockLabel) {
						newBlockOption.text(blockLabel);
					}));
					ItemStoredTransaction.Subtract.fromBlockSelect.append(newBlockOption);
				});
			}
			if (fromStored) {
				console.debug("Showing from stored");
				ItemStoredTransaction.Subtract.fromStoredContainer.show();
				promises.push(ItemStoredSearchSelect.setupInputs(
					ItemStoredTransaction.Subtract.fromStoredInputGroup,
					item,
					stored
				));
			}
			if (amount) {
				console.debug("Showing amount inputs");
				ItemStoredTransaction.Subtract.amountContainer.show();
			}

			await Promise.all(promises);

			if (typeSelect) {
				ItemStoredTransaction.Subtract.typeUpdated(true);
			}
			if (amount) {
				ItemStoredTransaction.Subtract.updateAmount(item, stored, null, true);
			}

			Main.processStop();
		},
		typeUpdated(force = false) {
			if (force || ItemStoredTransaction.Subtract.typeSelect.is(":visible")) {
				console.log("Updating inputs based on selected subtract type.");

				let newType = ItemStoredTransaction.Subtract.typeSelect.val();
				switch (newType) {
					case("SUBTRACT_WHOLE"):
						ItemStoredTransaction.Subtract.amountContainer.hide();
						// ItemStoredTransaction.Subtract.fromStoredContainer.show();
						// ItemStoredTransaction.Subtract.fromBlockContainer.hide();
						break;
					case("SUBTRACT_AMOUNT"):
						ItemStoredTransaction.Subtract.amountContainer.show();
						// ItemStoredTransaction.Subtract.fromStoredContainer.hide();
						// ItemStoredTransaction.Subtract.fromBlockContainer.show();
						ItemStoredTransaction.Subtract.updateAmount(null, null, null, true);
						break;
				}
			}
		},
		updateAmount: async function (item = null, stored = null, storageBlockId = null, force = false) {
			if (force || ItemStoredTransaction.Subtract.amountInputsContainer.is(":visible")) {
				console.log("Updating amounts");

				if (item == null) {
					item = ItemStoredTransaction.Subtract.itemIdInput.val();
					console.debug("Got item id from form: ", item)
				}
				if (typeof item === 'string' || item instanceof String) {
					Getters.InventoryItem.get(item, function (itemData) {
						ItemStoredTransaction.Subtract.updateAmount(itemData, stored, storageBlockId, force);
					});
					return;
				}

				if (stored != null) {
					console.log("Stored specified: ", stored)
					if (stored === -1) {
						stored = null;
					} else {
						if (typeof stored === 'string' || stored instanceof String) {
							await Getters.StoredItem.getStored(item.id, stored, function (storedData) {
								stored = storedData;
							});
						}
					}
					console.log("final stored for amount input generation: ", stored)
					StoredFormInput.getAmountInputs(item, stored, true, true).then(function (inputs) {
						ItemStoredTransaction.Subtract.amountInputsContainer.html(inputs);
						ItemStoredTransaction.Subtract.updateAllAmount();
					});
				} else {//find stored
					console.log("Stored not specified. Gleaning from form.");
					if (storageBlockId == null && ItemStoredTransaction.Subtract.fromBlockSelect.is(":visible")) {
						storageBlockId = ItemStoredTransaction.Subtract.fromBlockSelect.val();
						console.debug("Got storage block id from block select: ", storageBlockId);
					}
					if (storageBlockId == null && ItemStoredTransaction.Subtract.fromStoredBlockIdInput.is(":visible")) {
						storageBlockId = ItemStoredTransaction.Subtract.fromStoredBlockIdInput.val();
						console.debug("Got storage block id from stored select: ", storageBlockId);
					}
					if (stored == null && ItemStoredTransaction.Subtract.fromStoredStoredIdInput.is(":visible")) {
						stored = ItemStoredTransaction.Subtract.fromStoredStoredIdInput.val();
						console.debug("Got stored id from form: ", stored);
					}

					console.log("Stored/ Block: ", stored, storageBlockId);

					if (!stored && !storageBlockId) {
						console.log("No stored or storage block id could be identified.");
						StoredFormInput.getAmountInputs(item, null, true, true).then(function (inputs) {
							ItemStoredTransaction.Subtract.amountInputsContainer.html(inputs);
							ItemStoredTransaction.Subtract.updateAllAmount();
						});
						return;
					}

					if (stored != null) {
						ItemStoredTransaction.Subtract.updateAmount(item, stored, storageBlockId, force);
						return;
					}
					if (storageBlockId != null) {
						Getters.StoredItem.getSingleStoredForItemInBlock(item.id, storageBlockId, function (storedData) {
								ItemStoredTransaction.Subtract.updateAmount(item, storedData, storageBlockId, force);
							},
							function () {
								ItemStoredTransaction.Subtract.updateAmount(item, -1, storageBlockId, force)
							}
						);
						return;
					}
					throw new Error("Should not be able to get here.");
				}
			} else {
				console.debug("Amounts not visible; not updating.");
			}
		},
		updateAllAmount() {
			let inputs = ItemStoredTransaction.Subtract.amountInputsContainer.find("input, select");
			if (ItemStoredTransaction.Subtract.amountSubtractAllInput.is(":checked")) {
				inputs.prop("disabled", true);
			} else {
				inputs.prop("disabled", false);
			}
		},
		submitFormHandler: async function (event) {
			event.preventDefault();
			console.log("Building and submitting subtract transaction.");
			let transaction = {};

			if (ItemStoredTransaction.Subtract.amountInputsContainer.is(":visible")) {
				transaction['type'] = "SUBTRACT_AMOUNT";
				if (ItemStoredTransaction.Subtract.amountSubtractAllInput.is(":checked")) {
					transaction['all'] = true;
				} else {
					transaction['amount'] = UnitUtils.getQuantityFromInputs(ItemStoredTransaction.Subtract.amountInputsContainer);
				}
			} else {
				transaction['type'] = "SUBTRACT_WHOLE";
			}

			if (ItemStoredTransaction.Subtract.fromBlockContainer.is(":visible")) {
				transaction['fromBlock'] = ItemStoredTransaction.Subtract.fromBlockSelect.val();
			}

			if (ItemStoredTransaction.Subtract.fromStoredContainer.is(":visible")) {
				transaction[
					transaction['type'] === "SUBTRACT_AMOUNT" ? 'fromStored' : 'toSubtract'
					] = ItemStoredTransaction.Subtract.fromStoredStoredIdInput.val();
			}

			console.log("Built subtract transaction object: ", transaction);
			await ItemStoredTransaction.submitTransaction(
				ItemStoredTransaction.Subtract.itemIdInput.val(),
				transaction,
				ItemStoredTransaction.Subtract.modal
			);
		}
	},
	Transfer: {
		modal: $("#itemStoredTransactionTransferModal"),
		messages: $("#itemStoredTransactionTransferMessages"),
		form: $("#itemStoredTransactionTransferForm"),

		itemInputContainer: $("#itemStoredTransactionTransferFormItemInputContainer"),
		itemSearchIdInput: $("#itemStoredTransactionTransferFormItem-itemInputId"),
		itemInputClearButton: $("#itemStoredTransactionTransferFormItem-itemInputClearButton"),

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
		fromStoredBlockIdInput: $("#itemStoredTransactionTransferFormFromItemStored-itemStoredInputBlockId"),
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

		resetForm: function (triggerItemIdChange = true) {
			ItemStoredTransaction.Transfer.itemInputContainer.hide();
			ItemSearchSelect.clearSearchInput(ItemStoredTransaction.Transfer.itemInputClearButton, triggerItemIdChange);

			ItemStoredTransaction.Transfer.itemInfoContainer.hide();
			ItemStoredTransaction.Transfer.itemInfoItemName.text("");

			ItemStoredTransaction.Transfer.storedInfoContainer.hide();

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
		setupForm: async function (item, stored, buttonElement = null) {
			Main.processStart();
			if (buttonElement != null) {
				ModalUtils.setReturnModal(this.modal, buttonElement);
			}
			ItemStoredTransaction.Transfer.resetForm(false);
			if (item == null && stored == null) {
				console.log("No item given.")
				ItemStoredTransaction.Transfer.itemInputContainer.show();
				Main.processStop();
				return;
			}

			if (typeof item === "string" || (item instanceof String)) {
				console.log("Got item id")
				return Getters.InventoryItem.get(item, function (itemData) {
					ItemStoredTransaction.Transfer.setupForm(itemData, stored, buttonElement);
					Main.processStop();
				});
			}

			if (typeof stored === "string" || (stored instanceof String)) {
				console.log("Got stored id")
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

			promises.push(ItemStoredSearchSelect.setupInputs(ItemStoredTransaction.Transfer.fromStoredSelect, item, stored));
			promises.push(ItemStoredSearchSelect.setupInputs(ItemStoredTransaction.Transfer.toStoredSelect, item));

			let typeSelect = false;
			let fromBlock = false;
			let fromStored = false;
			let amount = false;
			let toBlock = false;
			let toStored = false;
			StorageTypeUtils.runForType(
				item,
				function () {
					fromBlock = true;
					toBlock = true;
					amount = true;
				},
				function () {
					typeSelect = true;
					fromStored = true;
					amount = true;
					toStored = true;
					toBlock = true;
				},
				function () {
					fromStored = true;
					toBlock = true;
				},
				function () {
					fromBlock = true;
					toBlock = true;
				}
			);

			//populate inputs that are used
			if (typeSelect) {
				console.debug("Showing type select");
				ItemStoredTransaction.Transfer.transactionTypeContainer.show();
			}
			if (fromBlock) {
				console.debug("Showing from block");
				ItemStoredTransaction.Transfer.fromBlockContainer.show();
				item.storageBlocks.forEach(function (blockId) {
					let newBlockOption = $('<option></option>');
					newBlockOption.val(blockId);
					promises.push(Getters.StorageBlock.getStorageBlockLabel(blockId, function (blockLabel) {
						newBlockOption.text(blockLabel);
					}));
					ItemStoredTransaction.Transfer.fromBlockSelect.append(newBlockOption);
				});
			}
			if (fromStored) {
				console.debug("Showing from stored");
				ItemStoredTransaction.Transfer.fromStoredContainer.show();
			}
			if (amount) {
				console.debug("Showing amount");
				ItemStoredTransaction.Transfer.amountInputContainer.show();

				promises.push(
					StoredFormInput.getAmountInputs(item, stored, true, true).then(function (inputs) {
						ItemStoredTransaction.Transfer.amountInputs.html(inputs);
					})
				);
			}
			if (toBlock) {
				console.debug("Showing to block");
				ItemStoredTransaction.Transfer.toBlockContainer.show();
				item.storageBlocks.forEach(function (blockId) {
					let newBlockOption = $('<option></option>');
					newBlockOption.val(blockId);
					promises.push(Getters.StorageBlock.getStorageBlockLabel(blockId, function (blockLabel) {
						newBlockOption.text(blockLabel);
					}));
					ItemStoredTransaction.Transfer.toBlockSelect.append(newBlockOption);
				});
			}
			if (toStored) {
				console.debug("Showing toStored");
				ItemStoredTransaction.Transfer.toStoredContainer.show();
			}

			await Promise.all(promises);

			if (fromBlock) {
				if (item.storageType === "UNIQUE_SINGLE") {
					console.log("Preventing selection of current block used.");

					await Getters.StoredItem.getSingleStoredForItem(
						item.id,
						function (storedInItem) {
							ItemStoredTransaction.Transfer.fromBlockSelect.find("option").each(function (i, option) {
								let optionJq = $(option);
								if (optionJq.val() !== storedInItem.storageBlock) {
									optionJq.prop("disabled", true);
									optionJq.prop("selected", false);
								}
							});
						}
					);

				}
			}

			if (toBlock) {
				console.debug("Updating toBlock to ensure valid.");
				let blockId;
				if (fromStored) {
					blockId = ItemStoredTransaction.Transfer.fromStoredBlockIdInput.val();
				}
				if (fromBlock) {
					blockId = ItemStoredTransaction.Transfer.fromBlockSelect.val()
				}
				ItemStoredTransaction.Transfer.updateToBlock(blockId, true);
			} else {
				console.debug("Not updating toBlock to ensure valid");
			}
			if (amount) {
				await ItemStoredTransaction.Transfer.updateAmount(null, null, null, true);
			}
			if (typeSelect) {
				ItemStoredTransaction.Transfer.typeChanged(true);
			}

			//TODO:: make & run update to block to not select same block as is selected in from
			Main.processStop();
		},
		/**
		 * Updates visibility of fields, selected/disabled in dropdowns based on what is selected
		 */
		typeChanged(force = false) {
			if (force || ItemStoredTransaction.Transfer.transactionTypeContainer.is(":visible")) {
				let type = ItemStoredTransaction.Transfer.transactionTypeInput.val();
				console.log("Transaction type changed: ", type);

				switch (type) {
					case "TRANSFER_WHOLE":
						ItemStoredTransaction.Transfer.amountInputContainer.hide();
						ItemStoredTransaction.Transfer.toBlockContainer.show();
						ItemStoredTransaction.Transfer.toStoredContainer.hide();
						ItemStoredTransaction.Transfer.updateToBlock(ItemStoredTransaction.Transfer.fromStoredBlockIdInput.val());
						break;
					case "TRANSFER_AMOUNT":
						ItemStoredTransaction.Transfer.amountInputContainer.show();
						ItemStoredTransaction.Transfer.toBlockContainer.hide();
						ItemStoredTransaction.Transfer.toStoredContainer.show();
						ItemStoredTransaction.Transfer.updateAmount();
						break;
					default:
						throw new Error("Invalid value for transfer type: " + type);
				}
			}
		},
		updateToBlock(selectedBlockId = null, force = false) {
			//TODO:: update for from stored
			if (force || ItemStoredTransaction.Transfer.toBlockSelect.is(":visible")) {
				console.debug("Updating availability of to block options. Block to make unavailable: ", selectedBlockId);
				ItemStoredTransaction.Transfer.toBlockSelect.find("option").each(function (i, option) {
					$(option).prop("disabled", false);
				});

				if (selectedBlockId != null && selectedBlockId) {
					let toDisable = ItemStoredTransaction.Transfer.toBlockSelect.find("option[value=" + selectedBlockId + "]");
					toDisable.prop("disabled", true);
					toDisable.prop("selected", false);
				}
			} else {
				console.debug("ToBlock not visible; not updating.");
			}
		},
		/**
		 * Updates the amount input to the value of what is present in the stored selected.
		 */
		updateAmount: async function (item = null, stored = null, storageBlockId = null, force = false) {
			if (force || ItemStoredTransaction.Transfer.amountInputContainer.is(":visible")) {
				console.log("Updating amounts");

				if (item == null) {
					item = ItemStoredTransaction.Transfer.itemIdInput.val();
				}
				if (typeof item === 'string' || item instanceof String) {
					Getters.InventoryItem.get(item, function (itemData) {
						ItemStoredTransaction.Transfer.updateAmount(itemData, stored, storageBlockId, force);
					});
					return;
				}

				if (stored != null) {
					console.log("Stored specified: ", stored)
					if (stored === -1) {
						stored = null;
					} else {
						if (typeof stored === 'string' || stored instanceof String) {
							await Getters.StoredItem.getStored(item.id, stored, function (storedData) {
								stored = storedData;
							});
						}
					}
					console.log("final stored for amount input generation: ", stored)
					StoredFormInput.getAmountInputs(item, stored, true, true).then(function (inputs) {
						ItemStoredTransaction.Transfer.amountInputs.html(inputs);
						ItemStoredTransaction.Transfer.updateAllAmount();
					});
				} else {//find stored
					console.log("Stored not specified. Gleaning from form.");
					if (storageBlockId == null && ItemStoredTransaction.Transfer.fromBlockSelect.is(":visible")) {
						storageBlockId = ItemStoredTransaction.Transfer.fromBlockSelect.val();
						console.debug("Got storage block id from block select: ", storageBlockId);
					}
					if (storageBlockId == null && ItemStoredTransaction.Transfer.fromStoredBlockIdInput.is(":visible")) {
						storageBlockId = ItemStoredTransaction.Transfer.fromStoredBlockIdInput.val();
						console.debug("Got storage block id from stored select: ", storageBlockId);
					}
					if (stored == null && ItemStoredTransaction.Transfer.fromStoredStoredIdInput.is(":visible")) {
						stored = ItemStoredTransaction.Transfer.fromStoredStoredIdInput.val();
						console.debug("Got stored id from form: ", stored);
					}

					console.log("Stored/ Block: ", stored, storageBlockId);

					if (!stored && !storageBlockId) {
						console.log("No stored or storage block id could be identified.");
						return;
					}

					if (stored != null) {
						ItemStoredTransaction.Transfer.updateAmount(item, stored, storageBlockId, force);
						return;
					}
					if (storageBlockId != null) {
						Getters.StoredItem.getSingleStoredForItemInBlock(item.id, storageBlockId, function (storedData) {
								ItemStoredTransaction.Transfer.updateAmount(item, storedData, storageBlockId, force);
							},
							function () {
								ItemStoredTransaction.Transfer.updateAmount(item, -1, storageBlockId, force)
							}
						);
						return;
					}
					throw new Error("Should not be able to get here.");
				}
			} else {
				console.debug("Amounts not visible; not updating.");
			}
		},
		updateAllAmount() {
			let inputs = ItemStoredTransaction.Transfer.amountInputs.find("input, select");
			if (ItemStoredTransaction.Transfer.amountTransferAllInput.is(":checked")) {
				inputs.prop("disabled", true);
			} else {
				inputs.prop("disabled", false);
			}
		},
		submitFormHandler: async function (event) {
			event.preventDefault();
			let transaction = {};

			//TODO:: simple validation: to/from same stored
			//TODO:: fill out transaction

			if (ItemStoredTransaction.Transfer.amountInputContainer.is(":visible")) {
				transaction['type'] = "TRANSFER_AMOUNT";
				if (ItemStoredTransaction.Transfer.amountTransferAllInput.is(":checked")) {
					transaction['all'] = true;
				} else {
					transaction['amount'] = UnitUtils.getQuantityFromInputs(ItemStoredTransaction.Transfer.amountInputs);
				}
			} else {
				transaction['type'] = "TRANSFER_WHOLE";
			}

			if (ItemStoredTransaction.Transfer.fromBlockContainer.is(":visible")) {
				transaction['fromBlock'] = ItemStoredTransaction.Transfer.fromBlockSelect.val();
			}

			if (ItemStoredTransaction.Transfer.fromStoredContainer.is(":visible")) {
				transaction[
					transaction['type'] === "TRANSFER_WHOLE" ? 'storedToTransfer' : 'fromStored'
					] = ItemStoredTransaction.Transfer.fromStoredStoredIdInput.val();
			}

			if (ItemStoredTransaction.Transfer.toBlockContainer.is(":visible")) {
				transaction['toBlock'] = ItemStoredTransaction.Transfer.toBlockSelect.val();
			}

			if (ItemStoredTransaction.Transfer.toStoredContainer.is(":visible")) {
				transaction['toStored'] = ItemStoredTransaction.Transfer.toStoredIdInput.val();
			}

			console.log("Built transfer transaction object: ", transaction);
			await ItemStoredTransaction.submitTransaction(
				ItemStoredTransaction.Transfer.itemIdInput.val(),
				transaction,
				ItemStoredTransaction.Transfer.modal
			);
		}
	},
	initPage: function () {
		if (ItemStoredTransaction.Add.form) {
			ItemStoredTransaction.Add.form.on("submit", ItemStoredTransaction.Add.submitFormHandler);
			ItemStoredTransaction.Add.itemIdInput.on("change", function () {
				let itemId = ItemStoredTransaction.Add.itemIdInput.val();
				console.log("Got item for add transaction form. Setting up: ", itemId);
				ItemStoredTransaction.Add.setupForm(itemId);
			});
		}

		if (ItemStoredTransaction.Checkout.form) {
			ItemStoredTransaction.Checkout.form.on("submit", ItemStoredTransaction.Checkout.submitFormHandler);
			ItemStoredTransaction.Checkout.itemSearchIdInput.on("change", function () {
				console.log("Selected new item.");
				let item = ItemStoredTransaction.Checkout.itemSearchIdInput.val();
				if (item != null) {
					ItemStoredTransaction.Checkout.setupForm(item);
				}
			});
		}

		if (ItemStoredTransaction.Set.form) {
			ItemStoredTransaction.Set.form.on("submit", ItemStoredTransaction.Set.submitFormHandler);
		}

		if (ItemStoredTransaction.Subtract.form) {
			ItemStoredTransaction.Subtract.form.on("submit", ItemStoredTransaction.Subtract.submitFormHandler);
			ItemStoredTransaction.Subtract.itemSearchIdInput.on("change", function () {
				console.log("Selected new item.");
				let item = ItemStoredTransaction.Subtract.itemSearchIdInput.val();

				if (item != null) {
					ItemStoredTransaction.Subtract.setupForm(item);
				}
			});
		}

		if (ItemStoredTransaction.Transfer.form) {
			ItemStoredTransaction.Transfer.form.on("submit", ItemStoredTransaction.Transfer.submitFormHandler);
			ItemStoredTransaction.Transfer.itemSearchIdInput.on("change", function () {
				console.log("Selected new item.");
				let item = ItemStoredTransaction.Transfer.itemSearchIdInput.val();

				if (item != null) {
					ItemStoredTransaction.Transfer.setupForm(item);
				}
			});
		}

		if (ItemStoredTransaction.Checkin.form) {
			ItemStoredTransaction.Checkin.form.on("submit", ItemStoredTransaction.Checkin.submitFormHandler);
		}
	}
};
