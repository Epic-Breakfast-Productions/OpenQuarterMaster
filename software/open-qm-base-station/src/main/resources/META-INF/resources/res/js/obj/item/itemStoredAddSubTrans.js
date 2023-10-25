

const ItemStoredAddSubTransfer = {
	//TODO:: move things here
	formMessages: $("#itemStoredAddSubTransFormMessages"),
	form: $("#itemStoredAddSubTransForm"),
	formItemImg: $("#itemStoredAddSubTransFormItemImg"),
	formItemNameLabel: $("#itemStoredAddSubTransFormItemNameLabel"),
	opSelect: $("#itemStoredAddSubTransFormOpSelect"),
	toFromExistingStoredCheckbox: $("#itemStoredAddSubTransFormToFromExistingStoredCheckbox"),
	toContainer: $("#itemStoredAddSubTransFormToContainer"),
	toSelect: $("#itemStoredAddSubTransFormToSelect"),
	toListFormContainer: $("#itemStoredAddSubTransFormToListFormContainer"),
	toStoredFormContainer: $("#itemStoredAddSubTransFormToAmountFormContainer"),
	fromContainer: $("#itemStoredAddSubTransFormFromContainer"),
	fromSelect: $("#itemStoredAddSubTransFormFromSelect"),
	fromListFormContainer: $("#itemStoredAddSubTransFormFromListFormContainer"),
	fromAmountFormContainer: $("#itemStoredAddSubTransFormFromAmountFormContainer"),


	resetToFromForms() {
		this.fromListFormContainer.text("");
		this.fromAmountFormContainer.text("");
		this.toListFormContainer.text("");
		this.toStoredFormContainer.text("");
	},
	resetForms() {
		this.formItemImg.attr("src", "");
		this.formMessages.text("");
		this.formItemNameLabel.text("");
		this.resetToFromForms();
	},

	disableExistingStoredOption() {
		this.toFromExistingStoredCheckbox.prop('checked', false);
		this.toFromExistingStoredCheckbox.prop('disabled', true);
	},
	enableExistingStoredOption() {
		this.toFromExistingStoredCheckbox.prop('disabled', false);

	},

	toFromExistingStoredCheckboxChecked() {
		return this.toFromExistingStoredCheckbox.is(":checked");
	},

	getStoredSelectBox(storedObj) {
		let output = $('<div class="col-2">' +
			'<div class="card" style="">\n' +
			'  <ul class="list-group list-group-flush">\n' +
			'    <li class="list-group-item text-center"><input type="checkbox" class="selectedStored" /></li>\n' +
			'    <li class="list-group-item storedInfo"></li>\n' +
			'  </ul>\n' +
			'</div></div>');

		output.find(".selectedStored").val(storedObj.id);
		output.find(".storedInfo").text(storedObj.labelText);
		return output;
	},
	addStoredSelectBoxes(storedWrapper, itemType, containerJq) {
		let storedList = [];

		StoredTypeUtils.foreachStoredType(
			itemType,
			function () {
				console.log("Somehow got to where we tried to show a list for simple.")
			},
			function () {
				storedList = storedWrapper.stored;
			},
			function () {
				//TODO
			}
		);

		if (!storedList.length) {
			containerJq.html('<div class="col-12 text-center">No Stored to select!</div>');
		} else {
			storedList.forEach(function (curStored) {
				containerJq.append(ItemStoredAddSubTransfer.getStoredSelectBox(curStored));
			});
		}
	},
	addStoredForm(storageType, containerJq, fullAmountForm = true) {
		let content = "";
		StoredTypeUtils.foreachStoredType(
			storageType,
			function () {
				content = StoredEdit.getAmountStoredFormElements(
					null, null, fullAmountForm
				);
			},
			function () {
				content = StoredEdit.getAmountStoredFormElements(
					null, null, fullAmountForm
				);
			},
			function () {
				content = StoredEdit.getTrackedStoredFormElements(
					null, null
				);
			}
		);
		containerJq.append(content);
	},

	setupForAdd() {
		console.log("Setting up add/sub/transfer for Add")
		let itemData = jQuery.data(ItemStoredAddSubTransfer.form, "curItem");
		this.toContainer.show();
		this.fromContainer.hide();

		StoredTypeUtils.foreachStoredType(
			itemData.storageType,
			function () {
				ItemStoredAddSubTransfer.addStoredForm(
					itemData.storageType,
					ItemStoredAddSubTransfer.toStoredFormContainer,
					false
				)
			},
			function () {
				if (ItemStoredAddSubTransfer.toFromExistingStoredCheckboxChecked()) {
					ItemStoredAddSubTransfer.addStoredSelectBoxes(
						itemData.storageMap[ItemStoredAddSubTransfer.toSelect.val()],
						itemData.storageType,
						ItemStoredAddSubTransfer.toListFormContainer
					)
				}
				ItemStoredAddSubTransfer.addStoredForm(
					itemData.storageType,
					ItemStoredAddSubTransfer.toStoredFormContainer,
					!ItemStoredAddSubTransfer.toFromExistingStoredCheckboxChecked()
				)
			},
			function () {
				ItemStoredAddSubTransfer.addStoredForm(
					itemData.storageType,
					ItemStoredAddSubTransfer.toStoredFormContainer
				)
			}
		);
	},
	setupForSubtract() {
		console.log("Setting up add/sub/transfer for Subtract")
		let itemData = jQuery.data(ItemStoredAddSubTransfer.form, "curItem");
		this.toContainer.hide();
		this.fromContainer.show();

		StoredTypeUtils.foreachStoredType(
			itemData.storageType,
			function () {
				ItemStoredAddSubTransfer.addStoredForm(
					itemData.storageType,
					ItemStoredAddSubTransfer.fromAmountFormContainer,
					false
				)
			},
			function () {
				if (ItemStoredAddSubTransfer.toFromExistingStoredCheckboxChecked()) {
					ItemStoredAddSubTransfer.addStoredForm(
						itemData.storageType,
						ItemStoredAddSubTransfer.fromAmountFormContainer,
						false
					)
				}
				ItemStoredAddSubTransfer.addStoredSelectBoxes(
					itemData.storageMap[ItemStoredAddSubTransfer.fromSelect.val()],
					itemData.storageType,
					ItemStoredAddSubTransfer.fromListFormContainer
				)
			},
			function () {
				ItemStoredAddSubTransfer.addStoredSelectBoxes(
					itemData.storageMap[ItemStoredAddSubTransfer.fromSelect.val()],
					itemData.storageType,
					ItemStoredAddSubTransfer.fromListFormContainer
				)
			}
		);
	},
	setupForTransfer() {
		console.log("Setting up add/sub/transfer for Transfer")
		let itemData = jQuery.data(ItemStoredAddSubTransfer.form, "curItem");
		this.toContainer.show();
		this.fromContainer.show();

		StoredTypeUtils.foreachStoredType(
			itemData.storageType,
			function () {
				ItemStoredAddSubTransfer.addStoredForm(
					itemData.storageType,
					ItemStoredAddSubTransfer.fromAmountFormContainer,
					false
				)
			},
			function () {
				if (ItemStoredAddSubTransfer.toFromExistingStoredCheckboxChecked()) {
					ItemStoredAddSubTransfer.addStoredForm(
						itemData.storageType,
						ItemStoredAddSubTransfer.fromAmountFormContainer,
						false
					);
					ItemStoredAddSubTransfer.addStoredSelectBoxes(
						itemData.storageMap[ItemStoredAddSubTransfer.toSelect.val()],
						itemData.storageType,
						ItemStoredAddSubTransfer.toListFormContainer
					);
				}
				ItemStoredAddSubTransfer.addStoredSelectBoxes(
					itemData.storageMap[ItemStoredAddSubTransfer.fromSelect.val()],
					itemData.storageType,
					ItemStoredAddSubTransfer.fromListFormContainer
				);
			},
			function () {
				ItemStoredAddSubTransfer.addStoredSelectBoxes(
					itemData.storageMap[ItemStoredAddSubTransfer.fromSelect.val()],
					itemData.storageType,
					ItemStoredAddSubTransfer.fromListFormContainer
				)
			}
		);
	},

	/**
	 * Sets up the to/from forms based on item and current form options
	 *
	 * Call when:
	 *   - Initial setup
	 *   - Action form item changes
	 *   - to/from existing stored form item changes
	 */
	setupToFromForm() {
		this.resetToFromForms();
		switch (this.opSelect.val()) {
			case "add":
				this.setupForAdd();
				break;
			case "subtract":
				this.setupForSubtract();
				break;
			case "transfer":
				this.setupForTransfer();
				break;
		}

		// console.log("To container visible? " + this.toContainer.is(":hidden"))
	},

	/**
	 * Sets up the form for a given item.
	 * @param itemId
	 */
	setupForItem(itemId) {
		this.resetForms();
		this.formItemImg.attr("src", "/api/v1/media/image/for/item/" + itemId);
		doRestCall({
			// spinnerContainer: null,
			url: "/api/v1/inventory/item/" + itemId,
			done: async function (itemData) {
				jQuery.data(ItemStoredAddSubTransfer.form, "curItem", itemData);
				ItemStoredAddSubTransfer.formItemNameLabel.text(itemData.name);
				let storageBLockIds = Object.keys(itemData.storageMap);
				console.log("Storage block ids: " + storageBLockIds);
				//TODO:: check for no block ids

				await ItemStoredAddSubTransfer.setupFromToSelects(storageBLockIds);

				StoredTypeUtils.foreachStoredType(
					itemData.storageType,
					function () {
						ItemStoredAddSubTransfer.disableExistingStoredOption();
					},
					function () {
						ItemStoredAddSubTransfer.enableExistingStoredOption();
					},
					function () {
						ItemStoredAddSubTransfer.disableExistingStoredOption();
					}
				);
				ItemStoredAddSubTransfer.setupToFromForm();
			}
		});
	},

	/**
	 * Sets up the select elements with the given storage block ids.
	 * @param storageBlockIds
	 * @param allowSelectSame
	 * @returns {Promise<void>}
	 */
	async setupFromToSelects(storageBlockIds, allowSelectSame = false) {
		ItemStoredAddSubTransfer.fromSelect.text("");
		ItemStoredAddSubTransfer.toSelect.text("");

		//TODO:: add to promises, wait for promises to complete
		let promises = [];

		storageBlockIds.forEach(function (curStorageBlockId) {
			promises.push(
				Promise.resolve(getStorageBlockLabel(curStorageBlockId, function (blockLabel) {
						let newOptionTo = $('<option></option>');
						newOptionTo.attr("value", curStorageBlockId);
						newOptionTo.text(blockLabel);

						let newOptionFrom = newOptionTo.clone(true, true);

						ItemStoredAddSubTransfer.fromSelect.append(newOptionFrom);
						ItemStoredAddSubTransfer.toSelect.append(newOptionTo);
					})
				)
			);
		});

		await Promise.all(promises);

		if (!allowSelectSame) {
			//TODO:: this
			//TODO:: check if only one block associated
			// probably doesn't go here, need code to handle not here
		}
	},

	submitAddSubTransForm(){
		console.log("Submitting add/sub/trans form")
	}
};

ItemStoredAddSubTransfer.form.on("submit", function (event){
	event.preventDefault();
	ItemStoredAddSubTransfer.submitAddSubTransForm();
});