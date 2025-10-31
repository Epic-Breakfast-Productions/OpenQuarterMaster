const IdGeneratorSearchSelect = {
	AssociatedInput: {
		getInputContainer(innerElementJq) {
			return innerElementJq.closest(".idGeneratorInput");
		},
		getAssociatedGeneratorList(idGenInputJq) {
			return idGenInputJq.find(".associatedIdentifierGeneratorTableContent");
		},
		associateButtonClicked: function (buttonJs) {
			console.debug("Clicked associate button.");
			let buttonJq = $(buttonJs);
			let idGenInputContainer = IdGeneratorSearchSelect.IdGenInput.getInputContainer(buttonJq);

			IdGeneratorSearchSelect.setupSearchSelect(
				idGenInputContainer,
				idGenInputContainer.data("generates"),
				idGenInputContainer.data("forobject")
			);
		},
		associateIdGenerator: function (idGenInputJq, idGenData) {

			//TODO:: skip if not proper type of id generator

			let newRow = $('<tr class="associatedIdentifierGenerator"></tr>');

			newRow.append($('<td class="idGeneratorName"></td>').text(idGenData.name));

			newRow.append(
				$('<td></td>')
					.append(
						$('<button type="button" class="btn btn-sm btn-outline-danger" onclick="$(this).closest(\'.associatedIdentifierGenerator\').remove()" title="Unassociate this ID generator"></button>')
							.html(Icons.remove)
					)
			);

			IdGeneratorSearchSelect.IdGenInput.getAssociatedGeneratorList(idGenInputJq).append(newRow);
		}
	},
	GenerateInput: {
		generateButtonClicked: function (buttonJs) {
			console.debug("Clicked generate button.");
			let buttonJq = $(buttonJs);

			IdGeneratorSearchSelect.setupSearchSelect(
				buttonJq,
				buttonJq.data("generates"),
				buttonJq.data("forobject")
			);
		},
		generateFor(generateButtonJq, idGeneratorData){
			console.log("Adding to generate");
			let generates = generateButtonJq.data("generates");
			// let newId = IdGeneratorSearchSelect.GenerateInput.newToGenerateId(idGeneratorData, generates);//TODO:: needed?

			let addedToGenerate = $(`
			<div class="col-xl-3 col-md-4 col-sm-6 col-xs-6 mb-1 toGenerateContainer">
				<div class="card identifierDisplay">
					<div class="card-body p-1">
						<div class="identifierValueContainer text-center">
							<p class="h4 card-title identifierValue text-nowrap user-select-all mb-0 ">
								`+Icons.idGenerators+`
								Generating
							</p>
							<p class=" text-secondary mb-1">
								Generating from:<br />
								`+Icons.idGenerators+`<span class="fromGenerator"></span>
							</p>
						</div>
						
						<div class="form-floating">
							<input type="text" class="form-control" name="label" aria-label="Identifier Label">
							<label>Identifier Label</label>
						</div>
						
						<div class="mt-1 d-flex justify-content-center">
							<div class="input-group m-1 p-1">
								<button type="button" title="Move identifier up" class="btn btn-sm btn-outline-dark moveUpButton"><i class="bi bi-chevron-left "></i></button>
								<button type="button" title="Move identifier down" class="btn btn-sm btn-outline-dark moveDownButton"><i class="bi bi-chevron-right "></i></button>
							</div>
							<button type="button" title="Remove this identifier" class="btn btn-sm btn-outline-danger removeButton"><i class="bi bi-trash-fill "></i></button>
						</div>
					</div>
					
				</div>
				
			</div>
			`);
			//TODO:: adjust added above
			addedToGenerate.find(".fromGenerator").data("generator", idGeneratorData.id).text(idGeneratorData.name);
			addedToGenerate.find("input[name=label]").val(idGeneratorData.label);



			switch(generates){
				case "UNIQUE":
					addedToGenerate.addClass("uniqueIdentifierContainer");

					//TODO:: add onclicks


					UniqueIdentifiers.getIdentifiersContainer(UniqueIdentifiers.getInputContainer(generateButtonJq)).append(addedToGenerate);
					break;
				case "GENERAL":
					addedToGenerate.addClass("generalIdentifierContainer");
					addedToGenerate.find(".moveUpButton").on("click", function(e){GeneralIdentifiers.moveUp($(this))});
					addedToGenerate.find(".moveDownButton").on("click", function(e){GeneralIdentifiers.moveDown($(this))});
					addedToGenerate.find(".removeButton").on("click", function(e){GeneralIdentifiers.removeIdentifier($(this))});

					GeneralIdentifiers.getIdentifiersContainer(GeneralIdentifiers.getInputContainer(generateButtonJq)).append(addedToGenerate);
					break;
				default:
					console.warn("Bad generates value");
			}
		},
	},

	searchSelectModal: $("#idGeneratorSearchSelectModal"),
	searchSelectForm: $("#idGeneratorSearchSelectForm"),
	searchSelectSearchResults: $("#idGeneratorSearchSelectResults"),
	newGeneratorForm: $("#idGeneratorSearchSelectNewGeneratorForm"),

	setupSearchSelect: function (idGenSelectAddInputJq, generates, forObject) {
		console.log("Setting up search select for: ", generates, forObject);

		ModalHelpers.setReturnModal(IdGeneratorSearchSelect.searchSelectModal, idGenSelectAddInputJq);

		let destinationId = idGenSelectAddInputJq.attr("id");
		IdGeneratorAddEdit.setupFormForAdd(IdGeneratorSearchSelect.newGeneratorForm, false, true);
		IdGeneratorSearchSelect.searchSelectModal.data("destinationId", destinationId);


		if (generates) {
			let generatesInput = IdGeneratorSearchSelect.searchSelectForm.find("select[name='generates']");
			generatesInput.val(generates);
			generatesInput.find(':selected').prop('disabled', false);
			generatesInput.find(':not(:selected)').prop('disabled', true);

			let addGenerates = IdGeneratorAddEdit.formGetters.generates(IdGeneratorSearchSelect.newGeneratorForm);
			addGenerates.val(generates);
			addGenerates.find(':selected').prop('disabled', false);
			addGenerates.find(':not(:selected)').prop('disabled', true);
		}
		if (forObject) {
			let forObjectInput = IdGeneratorSearchSelect.searchSelectForm.find("select[name='forObjectType']");
			forObjectInput.val(forObject);
			forObjectInput.find(':selected').prop('disabled', false);
			forObjectInput.find(':not(:selected)').prop('disabled', true);

			let addForObj = IdGeneratorAddEdit.formGetters.generatesFor(IdGeneratorSearchSelect.newGeneratorForm);
			addForObj.val(forObject);
			addForObj.find(':selected').prop('disabled', false);
			addForObj.find(':not(:selected)').prop('disabled', true);
			Dselect.setupDselect(addForObj[0]);
		}

		IdGeneratorSearchSelect.searchSelectForm.trigger("submit");
	},

	closeModal() {
		IdGeneratorSearchSelect.searchSelectModal.find(".btn-close").click();
	},

	selectIdGenerator: async function (idGenData) {
		console.log("Selected id generator: ", idGenData);
		if(typeof idGenData === 'string' || idGenData instanceof String){
			return Getters.Identifiers.generator(idGenData)
				.then(IdGeneratorSearchSelect.selectIdGenerator);
		}

		let destination = $("#" + IdGeneratorSearchSelect.searchSelectModal.data("destinationId"));

		if(destination.hasClass("idGeneratorGenerateButton")){
			IdGeneratorSearchSelect.GenerateInput.generateFor(destination, idGenData);
		} else if(destination.hasClass("")){//TODO: correct class to check
			IdGeneratorSearchSelect.AssociatedInput.associateIdGenerator(destination, idGenData);
		} else {
			console.warn("Destination of selected generator could not be determined.");
		}
	}
}


IdGeneratorSearchSelect.searchSelectForm.on("submit", function (event) {
	event.preventDefault();
	console.log("Submitting search form.");

	let searchParams = new URLSearchParams(new FormData(event.target));
	console.log("URL search params: " + searchParams);

	Rest.call({
		spinnerContainer: IdGeneratorSearchSelect.searchSelectModal.get(0),
		url: Rest.passRoot + "/identifier/generator?" + searchParams,
		returnType: "html",
		method: 'GET',
		failNoResponse: null,
		failNoResponseCheckStatus: true,
		extraHeaders: {
			"accept": "text/html",
			"actionType": "select",
			"searchFormId": "storageSearchSelectForm",
			"inputIdPrepend": IdGeneratorSearchSelect.searchSelectModal.attr("data-bs-inputIdPrepend"),
			"destinationId": IdGeneratorSearchSelect.searchSelectModal.data("destinationId"),
			"otherModalId": IdGeneratorSearchSelect.searchSelectModal.data("bs-othermodalid")
		},
		async: false,
		done: function (data) {
			console.log("Got data!");
			IdGeneratorSearchSelect.searchSelectSearchResults.html(data);
		}
	});
});
