const StoredFormInput = {
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
			'    <textarea class="form-control storedConditionNotesInput" name="conditionNotes"></textarea>\n' +
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
	getAmountInputs: async function (item, stored, showAmount = true) {
		console.log("Getting amount inputs");

		let output = $(
			'<div class="amountStoredFormElements">' +
			'</div>'
		);

		if (showAmount) {
			output.append($(
				'<label class="form-label">Amount:</label>\n' +
				'<div class="input-group mt-2 mb-3">\n' +
				'     <input type="number" class="form-control amountStoredValueInput" name="amountStored" placeholder="Value" value="0.00" min="0.00" step="any" required>\n' +
				'     <select class="form-select amountStoredUnitInput unitInput" name="amountStoredUnit"></select>\n' +
				'</div>'))
			//TODO:: amount value
			//TODO:: selected unit from stored
			let unitOps = await UnitUtils.getCompatibleUnitOptions(item.unit.string);
			output.find(".unitInput").append(unitOps);
		}

		return output;
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
	}
};
