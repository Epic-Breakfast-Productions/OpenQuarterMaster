const StoredFormInput = {
	getBasicInputs(stored, item) {
		let output = $(`
<div class="commonStoredFormElements">
	<div class="mb-3 ">
		<label class="form-label">Condition Percentage</label>
		<div class="input-group">
			<input type="number" max="100" min="0" step="any" class="form-control storedConditionPercentageInput" name="condition">
			<span class="input-group-text" id="addon-wrapping">%</span>
<!--				//TODO:: better label of better to worse-->
		</div>
	</div>
	<div class="mb-3">
		<label class="form-label">Condition Details</label>
		<textarea class="form-control storedConditionNotesInput" name="conditionNotes"></textarea>
	</div>
	<div class="mb-3">
		<label class="form-label">Expires</label>
		<input type="datetime-local" class="form-control storedExpiredInput" name="expires">
<!--		//TODO:: note to leave blank if not applicable-->
	</div>

	<div class="mb-3">
		<label class="form-label">
			` + Icons.generalIds + `
			General Ids
		</label>
		` + PageComponents.Inputs.GeneralIds.generalIdInput + `
	</div>
	
	<div class="mb-3">
		<label class="form-label">
			` + Icons.uniqueIds + `
			Unique Ids
		</label>
		` + PageComponents.Inputs.UniqueIds.uniqueIdInput + `
	</div>
	
	<div class="mb-3">
		<label class="form-label">
			` + Icons.pricing + `
			Pricing (adds to and overrides prices set on the item):
		</label>
		` + PageComponents.Inputs.Pricing.priceInput + `
	</div>
	
	` + PageComponents.Inputs.image + `
	` + PageComponents.Inputs.file + `
	` + PageComponents.Inputs.keywords + `
	` + PageComponents.Inputs.attribute + `
</div>`);

		let generalIdInputContainer = output.find(".generalIdInputContainer");
		let uniqueIdInputContainer = output.find(".uniqueIdInputContainer");


		if (stored != null) {
			if (stored.barcode) {
				output.find(".storedBarcodeInput").val(stored.barcode);
			}
			if (stored.condition) {
				output.find(".storedConditionPercentageInput").val(stored.condition);
			}
			if (stored.conditionNotes) {
				output.find(".storedConditionNotesInput").val(stored.conditionNotes);
			}
			if (stored.expires) {
				TimeHelpers.setDatetimelocalInput(
					output.find(".storedExpiredInput"),
					stored.expires
				);
			}

			Identifiers.getAssociateButton(generalIdInputContainer).data("forobject", "STORED").attr("id", "generatorSelect-" + window.crypto.getRandomValues(new Uint8Array(5)).join(""));
			UniqueIdentifiers.getAssociateButton(uniqueIdInputContainer).data("forobject", "STORED").attr("id", "generatorSelect-" + window.crypto.getRandomValues(new Uint8Array(5)).join(""));

			Identifiers.populateEdit(generalIdInputContainer, stored.generalIds, (item == null ? null : item.idGenerators));
			UniqueIdentifiers.populateEdit(uniqueIdInputContainer, stored.uniqueIds, (item == null ? null : item.idGenerators));
			KeywordAttEdit.addKeywordInputs(output.find(".keywordInputDiv"), stored.keywords);
			KeywordAttEdit.addAttInputs(output.find(".attInputDiv"), stored.attributes);
			ImageSearchSelect.addSelectedImages(output.find(".imagesSelected"), stored.imageIds);
			FileAttachmentSearchSelect.populateFileInputFromObject(output, stored.attachedFiles);
		}

		if (item !== null) {
			Identifiers.setupForAssociated(generalIdInputContainer, item.idGenerators);
			UniqueIdentifiers.setupForAssociated(uniqueIdInputContainer, item.idGenerators);

			let pricingInput = output.find(".pricingInput");
			Pricing.populateInput(
				pricingInput,
				item.unit.string,
				stored ? stored.prices : null
			);
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
			if (stored == null) {
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

			if (stored != null && maxFromStored) {
				console.debug("Setting up amount input to adapt to max specified by stored.")
				unitInput.on("change", function (event) {
					StoredFormInput.updateMaxAmount($(event.target));
				});
				unitInput.change();
			}
		}

		return output;
	},
	updateMaxAmount(unitInputJq) {
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
	getStoredInputs: async function (forStoredType, stored = null, item = null, forEdit = true) {
		let output = $('<div class="storedInputs"></div>');

		await StoredTypeUtils.runForType(forStoredType,
			async function () {
				let amountInputs = await StoredFormInput.getAmountInputs(item, stored, !forEdit);
				output.append(amountInputs);
			},
			function () {
				output.append(StoredFormInput.getUniqueInputs(item, stored));
			}
		);

		output.append(this.getBasicInputs(stored, item));

		return output;
	},
	dataFromInputs(dataToAddTo, containerJq) {
		//common inputs
		let commonInputsContainer = containerJq.find(".commonStoredFormElements");
		if (commonInputsContainer.length && commonInputsContainer.is(":visible")) {
			console.log("Had common form elements section.");
			dataToAddTo["identifiers"] = Identifiers.getIdentifierData(commonInputsContainer.find('.identifierInputContainer'));
			dataToAddTo["condition"] = commonInputsContainer.find('input[name="condition"]').val();
			dataToAddTo["conditionNotes"] = commonInputsContainer.find('textarea[name="conditionNotes"]').val();
			dataToAddTo["expires"] = TimeHelpers.getTsFromInput(commonInputsContainer.find('input[name="expires"]'));
			KeywordAttEdit.addKeywordAttData(dataToAddTo, commonInputsContainer.find(".keywordInputDiv"), commonInputsContainer.find(".attInputDiv"));
			ImageSearchSelect.addImagesToData(dataToAddTo, commonInputsContainer.find(".imagesSelected"));
			dataToAddTo["attachedFiles"] = FileAttachmentSearchSelect.getFileListFromInput(commonInputsContainer.find(".fileAttachmentSelectInputTableContent"));
			dataToAddTo["prices"] = Pricing.getPricingData(commonInputsContainer.find(".pricingInput"));
		}
		//amount inputs
		let amountInputsContainer = containerJq.find(".amountStoredFormElements");
		if (amountInputsContainer.length && amountInputsContainer.is(":visible")) {
			console.log("Had amount form elements section.");
			dataToAddTo["type"] = "AMOUNT";
			let amountStoredInput = amountInputsContainer.find(".amountStoredInput");
			if (amountStoredInput.length && amountStoredInput.is(":visible")) {
				console.log("Had amount form elements.");
				dataToAddTo.amount = UnitUtils.getQuantityFromInputs(amountInputsContainer);
			}
		}
		//unique inputs
		let uniqueInputsContainer = containerJq.find(".uniqueStoredFormInputs");
		if (uniqueInputsContainer.length && uniqueInputsContainer.is(":visible")) {
			console.log("Had unique form elements section.");
			dataToAddTo["type"] = "UNIQUE";
			//TODO
		}
	}
};
