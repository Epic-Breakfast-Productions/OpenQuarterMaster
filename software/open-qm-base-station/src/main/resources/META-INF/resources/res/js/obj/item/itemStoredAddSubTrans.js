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
	toFormContainer: $("#itemStoredAddSubTransFormToFormContainer"),
	fromContainer: $("#itemStoredAddSubTransFormFromContainer"),
	fromSelect: $("#itemStoredAddSubTransFormFromSelect"),
	fromFormContainer: $("#itemStoredAddSubTransFormFromFormContainer"),

	resetForms(){
		this.formItemImg.attr("src", "");
		this.formMessages.text("");
		this.formItemNameLabel.text("");
		this.fromFormContainer.text("");
	},

	disableExistingStoredOption(){
		this.toFromExistingStoredCheckbox.prop('checked', false);
		this.toFromExistingStoredCheckbox.prop('disabled', true);
	},
	enableExistingStoredOption(){
		this.toFromExistingStoredCheckbox.prop('disabled', false);

	},

	updateSelectedStoredInput(storedInputContainerJq){
		console.log("Updating to or from form elements in " + storedInputContainerJq.attr("id") + ". ");
		storedInputContainerJq.text("");
		if(!storedInputContainerJq.is(":visible")){
			console.log("Container not visible");
			return;
		}
		console.log("Container visible");
		let itemData = jQuery.data(ItemStoredAddSubTransfer.form, "curItem");

		let showStoredList = false;
		let showStoredForm = false;

		//TODO:: account for being in the to or from
		StoredTypeUtils.foreachStoredType(
			itemData.storageType,
			function (){
				showStoredList = false;
				showStoredForm = true;
			},
			function (){
				showStoredList = true;
				showStoredForm = ItemStoredAddSubTransfer.toFromExistingStoredCheckbox.is(":checked");
			},
			function (){
				showStoredList = true;
				showStoredForm = false;
			}
		);
		if(showStoredList){
			console.log("Showing stored list inputs");
			let storedListInputContent = $('<div class="row mt-1">List</div>');

			storedInputContainerJq.append(storedListInputContent);
		}
		if(showStoredForm){
			console.log("Showing stored inputs");
			let storedInputContent = $('<div class="row mt-1">Stored</div>');

			storedInputContainerJq.append(storedInputContent);
		}

	},

	updateAllSelectedStoredInput(){
		this.updateSelectedStoredInput(this.toFormContainer);
		this.updateSelectedStoredInput(this.fromFormContainer);
	},

	refreshStoredToFromInputs(){
		let itemData = jQuery.data(ItemStoredAddSubTransfer.form, "curItem");
		console.log("Updating add/sub/transfer form elements")

		switch (this.opSelect.val()){
			case "add":
				this.toContainer.show();
				this.fromContainer.hide();
				break;
			case "subtract":
				this.toContainer.hide();
				this.fromContainer.show();
				break;
			case "transfer":
				this.toContainer.show();
				this.fromContainer.show();
				break;
		}

		StoredTypeUtils.foreachStoredType(
			itemData.storageType,
			function (){
				ItemStoredAddSubTransfer.disableExistingStoredOption();
				//TODO
			},
			function (){
				ItemStoredAddSubTransfer.enableExistingStoredOption();
				//TODO
			},
			function (){
				ItemStoredAddSubTransfer.disableExistingStoredOption();
				//TODO
			}
		);

		this.updateAllSelectedStoredInput();
	},
	setupForItem(itemId){
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

				ItemStoredAddSubTransfer.setupFromToSelects(storageBLockIds);

				StoredTypeUtils.foreachStoredType(
					itemData.storageType,
					function (){
						ItemStoredAddSubTransfer.disableExistingStoredOption();
						//TODO
					},
					function (){
						ItemStoredAddSubTransfer.enableExistingStoredOption();
						//TODO
					},
					function (){
						ItemStoredAddSubTransfer.disableExistingStoredOption();
						//TODO
					}
				);
				ItemStoredAddSubTransfer.refreshStoredToFromInputs();
			}
		});
	},

	setupFromToSelects(storageBlockIds, allowSelectSame = false){
		ItemStoredAddSubTransfer.fromSelect.text("");
		ItemStoredAddSubTransfer.toSelect.text("");

		//TODO:: add to promises, wait for promises to complete
		let promises = [];

		storageBlockIds.forEach(function(curStorageBlockId){
			getStorageBlockLabel(curStorageBlockId, function (blockLabel){
				let newOptionTo = $('<option></option>');
				newOptionTo.attr("id", curStorageBlockId);
				newOptionTo.text(blockLabel);

				let newOptionFrom = newOptionTo.clone(true, true);

				ItemStoredAddSubTransfer.fromSelect.append(newOptionFrom);
				ItemStoredAddSubTransfer.toSelect.append(newOptionTo);
			});
		});

		if(!allowSelectSame){
			//TODO:: this
			//TODO:: check if only one block associated
		}
	}


};