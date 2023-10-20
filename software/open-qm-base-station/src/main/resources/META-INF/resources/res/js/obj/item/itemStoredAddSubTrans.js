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
		let output = $('<div class="col-1">' +
			'<div class="card" style="">\n' +
			'  <ul class="list-group list-group-flush">\n' +
			'    <li class="list-group-item text-center"><input type="checkbox" class="selectedStored" /></li>\n' +
			'    <li class="list-group-item storedInfo"></li>\n' +
			'  </ul>\n' +
			'</div></div>');

		output.find(".selectedStored").val(storedObj.id);
		output.find(".storedInfo").val(storedObj.labelText);
		return output;
	},
	addStoredSelectBoxes(storedList, containerJq) {
		//TODO:: handle none scenario
		storedList.forEach(function (curStored) {
			containerJq.append(ItemStoredAddSubTransfer.getStoredSelectBox(curStored));
		});
	},
	addStoredForm(storageType, containerJq) {
		containerJq.text(storageType + " form")
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
					ItemStoredAddSubTransfer.toStoredFormContainer
				)
			},
			function () {
				if (ItemStoredAddSubTransfer.toFromExistingStoredCheckboxChecked()) {
					ItemStoredAddSubTransfer.addStoredSelectBoxes(
						itemData.storageMap[ItemStoredAddSubTransfer.toSelect.val()],
						ItemStoredAddSubTransfer.toListFormContainer
					)
				}
				ItemStoredAddSubTransfer.addStoredForm(
					itemData.storageType,
					ItemStoredAddSubTransfer.toStoredFormContainer
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
				//TODO show amount form in from
			},
			function () {
				//TODO:: show amount form in from
				//TODO:: if to/from existing, show existing stored list as well
			},
			function () {
				//TODO show tracked list in from
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
				//TODO show amount form in from
			},
			function () {
				//TODO show existing stored list in from
				//TODO:: if to/from existing, show list in to, amount form in from
				//TODO:: allow selecting the same storage block
			},
			function () {
				//TODO show tracked list in from
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
			done: function (itemData) {
				jQuery.data(ItemStoredAddSubTransfer.form, "curItem", itemData);
				ItemStoredAddSubTransfer.formItemNameLabel.text(itemData.name);
				let storageBLockIds = Object.keys(itemData.storageMap);
				console.log("Storage block ids: " + storageBLockIds);
				//TODO:: check for no block ids

				ItemStoredAddSubTransfer.setupFromToSelects(storageBLockIds).then(r => {
				});

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
						newOptionTo.attr("id", curStorageBlockId);
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
	}


};