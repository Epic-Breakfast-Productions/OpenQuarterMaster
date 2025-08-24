const StoredFormInput = {
	getBasicInputs(stored) {
		//TODO:: update to use barcode input
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
			'        <span class="input-group-text" id="addon-wrapping">%</span>\n' +
			//TODO:: better label of better to worse
			'    </div>\n' + '</div>\n' + '<div class="mb-3">\n' +
			'    <label class="form-label">Condition Details</label>\n' +
			'    <textarea class="form-control storedConditionNotesInput" name="conditionNotes"></textarea>\n' +
			'</div>\n' +
			'<div class="mb-3">\n' +
			'    <label class="form-label">Expires</label>\n' +
			'    <input type="datetime-local" class="form-control storedExpiredInput" name="expires">\n' +
			//TODO:: note to leave blank if not applicable
			'</div>\n' + //TODO:: move these templates to js calls
			// imageInputTemplate.html() +

			//TODO:: add timezone note to expires

			PageComponents.Inputs.keywords +
			PageComponents.Inputs.attribute +
			//TODO:: images/files
			'</div>\n'
		);

		if(stored != null){
			if(stored.barcode){
				output.find(".storedBarcodeInput").val(stored.barcode);
			}
			if(stored.condition){
				output.find(".storedConditionPercentageInput").val(stored.condition);
			}
			if(stored.conditionNotes){
				output.find(".storedConditionNotesInput").val(stored.conditionNotes);
			}
			if(stored.expires){
				output.find(".storedExpiredInput").val(stored.expires);
			}
			KeywordAttEdit.addKeywordInputs(output.find(".keywordInputDiv"), stored.keywords);
			KeywordAttEdit.addAttInputs(output.find(".attInputDiv"), stored.attributes);
		}

		return output;
	},
	getAmountInputs: async function (item, stored, showAmount = true, maxFromStored = false) {
		console.log("Getting amount inputs");

		let output = $(
			'<div class="amountStoredFormElements">' +
			'</div>'
		);

		if (showAmount) {
			output.append($(
				'<label class="form-label">Amount:</label>\n' +
				'<div class="input-group mt-2 mb-3 amountStoredInput">\n' +
				'     <input type="number" class="form-control amountStoredValueInput" name="amountStored" placeholder="Value" value="0.00" min="0.00" step="any" required>\n' +
				'     <select class="form-select amountStoredUnitInput unitInput" name="amountStoredUnit"></select>\n' +
				'</div>'));

			let unitOps = null;
			let unit = null;
			if(stored == null){
				console.debug("No stored given basing units off of item.");
				unit = item.unit.string;
				unitOps = await UnitUtils.getCompatibleUnitOptions(
					unit
				);
			} else {
				console.debug("Stored given, basing units off ot that.");
				let amountValueInput = output.find(".amountStoredValueInput");
				amountValueInput.val(stored.amount.value);
				amountValueInput.attr("data-originalValue", JSON.stringify(stored.amount));

				unit = stored.amount.unit.string;
				unitOps = await UnitUtils.getCompatibleUnitOptions(stored);
			}
			let unitInput = output.find(".unitInput");
			unitInput.append(unitOps).val(unit);

			if(stored != null && maxFromStored){
				console.debug("Setting up amount input to adapt to max specified by stored.")
				unitInput.on("change", function (event) {
					StoredFormInput.updateMaxAmount($(event.target));
				});
				unitInput.change();
			}
		}

		return output;
	},
	updateMaxAmount(unitInputJq){
		console.log("Updating max value for amount input.");
		let amountStoredInputGroup = unitInputJq.parent();
		let amountStoredValueInput = amountStoredInputGroup.find(".amountStoredValueInput");
		amountStoredValueInput.attr("max", unitInputJq.find(":selected").attr("data-max-value"));
	},
	getUniqueInputs(stored) {
		let output = $('<div class="uniqueStoredFormInputs"></div>');
		//TODO:: make inputs

		return output;
	},
	getStoredInputs: async function (forStoredType, stored = null, item = null, forEdit=true) {
		let output = $('<div class="storedInputs"></div>');

		StoredTypeUtils.runForType(forStoredType,
			async function () {
				let amountInputs = await StoredFormInput.getAmountInputs(item, stored, !forEdit);
				output.append(amountInputs);
			},
			function () {
				output.append(StoredFormInput.getUniqueInputs(item, stored));
			}
		);

		output.append(this.getBasicInputs(stored));

		return output;
	},
	dataFromInputs(dataToAddTo, containerJq){
		//common inputs
		let commonInputsContainer = containerJq.find(".commonStoredFormElements");
		if(commonInputsContainer.length && commonInputsContainer.is(":visible")){
			console.log("Had common form elements section.");
			dataToAddTo["barcode"] = commonInputsContainer.find('input[name="barcode"]').val();
			dataToAddTo["condition"] = commonInputsContainer.find('input[name="condition"]').val();
			dataToAddTo["conditionNotes"] = commonInputsContainer.find('textarea[name="conditionNotes"]').val();
			dataToAddTo["expires"] = TimeHelpers.getTsFromInput(commonInputsContainer.find('input[name="expires"]'));
			KeywordAttEdit.addKeywordAttData(dataToAddTo, commonInputsContainer.find(".keywordInputDiv"), commonInputsContainer.find(".attInputDiv"));
		}
		//amount inputs
		let amountInputsContainer = containerJq.find(".amountStoredFormElements");
		if(amountInputsContainer.length && amountInputsContainer.is(":visible")){
			console.log("Had amount form elements section.");
			dataToAddTo["type"] = "AMOUNT";
			let amountStoredInput = amountInputsContainer.find(".amountStoredInput");
			if(amountStoredInput.length && amountStoredInput.is(":visible")){
				console.log("Had amount form elements.");
				dataToAddTo.amount = UnitUtils.getQuantityFromInputs(amountInputsContainer);
			}
		}
		//unique inputs
		let uniqueInputsContainer = containerJq.find(".uniqueStoredFormInputs");
		if(uniqueInputsContainer.length && uniqueInputsContainer.is(":visible")){
			console.log("Had unique form elements section.");
			dataToAddTo["type"] = "UNIQUE";
			//TODO
		}
	}
};
