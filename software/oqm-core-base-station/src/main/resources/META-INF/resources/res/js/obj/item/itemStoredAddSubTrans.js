

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
	fromStoredFormContainer: $("#itemStoredAddSubTransFormFromAmountFormContainer"),


	resetToFromForms() {
		this.fromListFormContainer.text("");
		this.fromStoredFormContainer.text("");
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

	getStoredSelectBox(storedObj, name) {
		let output = $('<div class="col-2">' +
			'<div class="card" style="">\n' +
			'  <ul class="list-group list-group-flush">\n' +
			'    <li class="list-group-item text-center"><input type="radio" class="selectedStored" required /></li>\n' +
			'    <li class="list-group-item storedInfo"></li>\n' +
			'  </ul>\n' +
			'</div></div>');

		output.find(".selectedStored").val(storedObj.id).prop("name", name);
		output.find(".storedInfo").text(storedObj.labelText);
		return output;
	},
	addStoredSelectBoxes(storedWrapper, itemType, containerJq, name) {
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
				containerJq.append(ItemStoredAddSubTransfer.getStoredSelectBox(curStored, name));
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
				//TODO:: update unit select with units compatible with unit
			},
			function () {
				content = StoredEdit.getAmountStoredFormElements(
					null, null, fullAmountForm
				);
				//TODO:: update unit select with units compatible with unit
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
						ItemStoredAddSubTransfer.toListFormContainer,
						"toStored"
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
					ItemStoredAddSubTransfer.fromStoredFormContainer,
					false
				)
			},
			function () {
				if (ItemStoredAddSubTransfer.toFromExistingStoredCheckboxChecked()) {
					ItemStoredAddSubTransfer.addStoredForm(
						itemData.storageType,
						ItemStoredAddSubTransfer.fromStoredFormContainer,
						false
					)
				}
				ItemStoredAddSubTransfer.addStoredSelectBoxes(
					itemData.storageMap[ItemStoredAddSubTransfer.fromSelect.val()],
					itemData.storageType,
					ItemStoredAddSubTransfer.fromListFormContainer,
					"fromStored"
				)
			},
			function () {
				ItemStoredAddSubTransfer.addStoredSelectBoxes(
					itemData.storageMap[ItemStoredAddSubTransfer.fromSelect.val()],
					itemData.storageType,
					ItemStoredAddSubTransfer.fromListFormContainer,
					"fromStored"
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
					ItemStoredAddSubTransfer.fromStoredFormContainer,
					false
				)
			},
			function () {
				if (ItemStoredAddSubTransfer.toFromExistingStoredCheckboxChecked()) {
					ItemStoredAddSubTransfer.addStoredForm(
						itemData.storageType,
						ItemStoredAddSubTransfer.fromStoredFormContainer,
						false
					);
					ItemStoredAddSubTransfer.addStoredSelectBoxes(
						itemData.storageMap[ItemStoredAddSubTransfer.toSelect.val()],
						itemData.storageType,
						ItemStoredAddSubTransfer.toListFormContainer,
						"toStored"
					);
				}
				ItemStoredAddSubTransfer.addStoredSelectBoxes(
					itemData.storageMap[ItemStoredAddSubTransfer.fromSelect.val()],
					itemData.storageType,
					ItemStoredAddSubTransfer.fromListFormContainer,
					"fromStored"
				);
			},
			function () {
				ItemStoredAddSubTransfer.addStoredSelectBoxes(
					itemData.storageMap[ItemStoredAddSubTransfer.fromSelect.val()],
					itemData.storageType,
					ItemStoredAddSubTransfer.fromListFormContainer,
					"fromStored"
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
		this.formItemImg.attr("src", Rest.passRoot + "/media/image/for/item/" + itemId);

		Rest.call({
			// spinnerContainer: null,
			url: Rest.passRoot + "/inventory/item/" + itemId,
			failMessagesDiv: ItemStoredAddSubTransfer.formMessages,
			done: async function (itemData) {
				UnitUtils.updateCompatibleUnits(itemData.unit.string);
				jQuery.data(ItemStoredAddSubTransfer.form, "curItem", itemData);
				jQuery.data(ItemStoredAddSubTransfer.form, "curItemType", itemData.storageType);
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

	/**
	 *
	 * @returns {({actionType: string}|*|string|jQuery)[]}
	 */
	buildAddSubTransActionObject(){
		console.log("Creating add sub trans apply object from form.")
		let output = {
			"actionType": this.opSelect.val().toUpperCase()
		};

		let fromStorageBlock = false;
		let toStorageBlock = false;

		let fromStorageId = false;
		let toStorageId = false;

		let fromStoredObj = false;
		let toStoredObj = false;

		let itemType = jQuery.data(ItemStoredAddSubTransfer.form, "curItemType");

		switch (this.opSelect.val()) {
			case "add":
				toStorageBlock = true;
				toStoredObj = true;
				StoredTypeUtils.foreachStoredType(
					itemType,
					function () {},
					function () {
						if (ItemStoredAddSubTransfer.toFromExistingStoredCheckboxChecked()) {
							toStorageId = true;
						}
					},
					function () {}
				);
				break;
			case "subtract":
				fromStorageBlock = true;

				StoredTypeUtils.foreachStoredType(
					itemType,
					function () {fromStoredObj = true;},
					function () {
						fromStorageId = true;
						if (ItemStoredAddSubTransfer.toFromExistingStoredCheckboxChecked()) {
							fromStoredObj = true;
						}
					},
					function () {fromStorageId = true;}
				);
				break;
			case "transfer":
				toStorageBlock = true;
				fromStorageBlock = true;
				StoredTypeUtils.foreachStoredType(
					itemType,
					function () {fromStoredObj = true;},
					function () {
						fromStorageId = true;
						if (ItemStoredAddSubTransfer.toFromExistingStoredCheckboxChecked()) {
							fromStoredObj = true;
							toStorageId = true;
						}
					},
					function () {fromStorageId = true;}
				);
				break;
		}

		if(fromStorageBlock){
			output['storageBlockFrom'] = this.fromSelect.val();
		}
		if(toStorageBlock){
			output['storageBlockTo'] = this.toSelect.val();
		}

		if(fromStorageId){
			output['storedIdFrom'] =  this.fromListFormContainer.find('input[name="fromStored"]:checked').val();
		}
		if(toStorageId){
			output['storedIdTo'] =  this.toListFormContainer.find('input[name="toStored"]:checked').val();
		}

		if(toStoredObj){
			output['toMove'] = StoredEdit.buildStoredObj(this.toStoredFormContainer, itemType);
		}
		if(fromStoredObj){
			output['toMove'] = StoredEdit.buildStoredObj(this.fromStoredFormContainer, itemType);
		}

		console.log("Apply object: ", output);
		return [output, jQuery.data(ItemStoredAddSubTransfer.form, "curItem")['id']];
	},

	addSubTransFormSubmitAction(applyObject, itemId){
		console.log("Applying item add/sub/transfer object.")
		Rest.call({
			// spinnerContainer: null,
			url: Rest.passRoot + "/inventory/item/" + itemId + "/stored/applyAddSubtractTransfer",
			method: "PUT",
			data: applyObject,
			failMessagesDiv: ItemStoredAddSubTransfer.formMessages,
			done: async function (itemData) {
				PageMessages.addMessageToDiv(
					ItemStoredAddSubTransfer.formMessages,
					"success",
					"Successfully performed action!",
					"Success"
				);
			}
		});
	}
};

ItemStoredAddSubTransfer.form.on("submit", function (event){
	event.preventDefault();
	console.log("Add/sub/trans form submitted")
	let [applyObject, itemId] = ItemStoredAddSubTransfer.buildAddSubTransActionObject();
	ItemStoredAddSubTransfer.addSubTransFormSubmitAction(applyObject, itemId);
	console.log("Done handling Add/sub/trans form submission handling.");
});