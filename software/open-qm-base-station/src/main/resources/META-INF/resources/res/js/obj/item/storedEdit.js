const StoredEdit = {
	getCommonStoredFormElements(headerId = null, toRemoveId = null) {
		//TODO:: add elements from params in a safe way
		return $('<div class="storedEditCommonFields">' +
			'<div class="mb-3 ">\n' +
			'    <label class="form-label">Barcode</label>\n' +
			'    <div class="input-group">\n' +
			'        <input type="text" class="form-control storedBarcodeInput" name="barcode" placeholder="UPC, ISBN...">\n' +
			'    </div>\n' +
			'</div>\n' +
			'<div class="mb-3 ">\n' +
			'    <label class="form-label">Condition Percentage</label>\n' +
			'    <div class="input-group">\n' +
			'        <input type="number" max="100" min="0" step="any" class="form-control storedConditionPercentageInput" name="condition" ' + (headerId == null ? '' : 'onchange="addEditUpdateStoredHeader(\'' + headerId + '\')"') + '>\n' + //TODO:: better label of better to worse
			'        <span class="input-group-text" id="addon-wrapping">%</span>\n' + //TODO:: better label of better to worse
			'    </div>\n' +
			'</div>\n' +
			'<div class="mb-3">\n' +
			'    <label class="form-label">Condition Details</label>\n' +
			'    <textarea class="form-control" name="conditionDetails"></textarea>\n' +
			'</div>\n' +
			'<div class="mb-3">\n' +
			'    <label class="form-label">Expires</label>\n' +
			'    <input type="date" class="form-control storedExpiredInput" name="expires" ' + (headerId == null ? '' : 'onchange="addEditUpdateStoredHeader(\'' + headerId + '\')"') + '>\n' + //TODO:: enforce future date?
			//TODO:: note to leave blank if not applicable
			'</div>\n' +
			//TODO:: move these templates to js calls
			imageInputTemplate.html() +
			//TODO:: show kw/att on same row. images too?
			keywordInputTemplate.html() +
			attInputTemplate.html() +
			(
				toRemoveId != null ?
			'<div class="mb-3 ">\n' +
			'    <button type="button" class="btn btn-danger" onclick="removeStored(\'#' + toRemoveId + '\');">' + Icons.remove + ' Remove Stored</button> ' +
			'</div>' :
					""
			) +
			'</div>\n')
			;
	},

	/**
	 *
	 * @param headerId
	 * @param toRemoveId
	 * @returns jQuery
	 */
	getAmountStoredFormElements(headerId, toRemoveId) {
		//TODO:: add elements from params in a safe way
		let output = $('<div class="amountStoredFormElements">' +
			'<div class="input-group mt-2 mb-3">\n' +
			'     <input type="number" class="form-control amountStoredValueInput" name="amountStored" placeholder="Value" value="0.00" min="0.00" step="any" required onchange="addEditUpdateStoredHeader(\'' + headerId + '\')">\n' +
			'     <select class="form-select amountStoredUnitInput unitInput" name="amountStoredUnit" onchange="addEditUpdateStoredHeader(\'' + headerId + '\')">' + ItemAddEdit.compatibleUnitOptions + '</select>\n' + //TODO:: populate
			'</div>' +
			'</div>');
		output.append(this.getCommonStoredFormElements(headerId, toRemoveId));
		return output;
	},

	/**
	 *
	 * @param headerId
	 * @param toRemoveId
	 * @returns jQuery
	 */
	getTrackedStoredFormElements(headerId, toRemoveId) {
		//TODO:: add elements from params in a safe way
		let output = $('<div class="mb-3">\n' +
			'    <label class="form-label">Identifier:</label>\n' +
			'    <input class="form-control" type="text" name="identifier" onchange="addEditUpdateStoredHeader(\'' + headerId + '\')" required>\n' + // TODO:: populate
			'</div>\n' +
			'<div class="mb-3">\n' +
			'    <label class="form-label">Identifying Details</label>\n' +
			'    <textarea class="form-control" name="identifyingDetails"></textarea>\n' +
			'</div>');
		output.append(this.getCommonStoredFormElements(headerId, toRemoveId));
		return output;
	}
};