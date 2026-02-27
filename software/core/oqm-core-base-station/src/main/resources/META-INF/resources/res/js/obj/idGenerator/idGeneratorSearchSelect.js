const IdGeneratorSearchSelect = {
	AssociatedInput: {
		getInputContainer(innerElementJq) {
			return innerElementJq.closest(".idGeneratorAssociatedInput");
		},
		getAssociatedGeneratorList(idGenInputJq) {
			return idGenInputJq.find(".associatedIdentifierGeneratorTableContent");
		},
		associateButtonClicked: function (buttonJs) {
			console.debug("Clicked associate button.");
			let buttonJq = $(buttonJs);
			let idGenInputContainer = IdGeneratorSearchSelect.AssociatedInput.getInputContainer(buttonJq);

			IdGeneratorSearchSelect.setupSearchSelect(
				idGenInputContainer,
				idGenInputContainer.data("for-object")
			);
		},
		associateIdGenerator: function (idGenInputJq, idGenData) {
			let associatedGeneratorList = IdGeneratorSearchSelect.AssociatedInput.getAssociatedGeneratorList(idGenInputJq);

			if(associatedGeneratorList.find(`[data-id='${idGenData.id}']`).length > 0) {
				console.log("Already selected id generator.")
				return;
			}

			let newRow = $('<tr class="associatedIdentifierGenerator"></tr>');

			newRow.append($('<td class="idGeneratorName"></td>')
				.text(idGenData.name))
				.attr("data-id", idGenData.id);

			newRow.append(
				$('<td></td>')
					.append(
						$('<button type="button" class="btn btn-sm btn-outline-danger" onclick="$(this).closest(\'.associatedIdentifierGenerator\').remove()" title="Unassociate this ID generator"></button>')
							.html(Icons.remove)
					)
			);

			associatedGeneratorList.append(newRow);
		},
		resetAssociatedIdGenListData: function (idGenInputJq) {
			IdGeneratorSearchSelect.AssociatedInput.getAssociatedGeneratorList(idGenInputJq).text("");
		},
		getAssociatedIdGenListData: function (idGenInputJq) {
			let output = [];

			IdGeneratorSearchSelect.AssociatedInput.getAssociatedGeneratorList(idGenInputJq).find("tr").each(function (i, curIdGenRow) {
				output.push($(curIdGenRow).data("id"));
			})

			return output;
		},
		populateAssociatedIdGenListData: function (idGenInputJq, list) {
			list.forEach(function (item) {
				Getters.Identifiers.generator(item).then(function (idGenData){
					IdGeneratorSearchSelect.AssociatedInput.associateIdGenerator(idGenInputJq, idGenData);
				});
			});
		}
	},
	GenerateInput: {
		generateButtonClicked: function (buttonJs) {
			console.debug("Clicked generate button: ", buttonJs);
			let buttonJq = $(buttonJs);

			let list = buttonJq.data("idGeneratorList");

			IdGeneratorSearchSelect.setupSearchSelect(
				buttonJq,
				buttonJq.data("for-object"),
				list
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

			addedToGenerate.find(".fromGenerator").data("generator", idGeneratorData.id).text(idGeneratorData.name);
			addedToGenerate.find("input[name=label]").val(idGeneratorData.label);

			addedToGenerate.addClass("addedIdentifierContainer");
			addedToGenerate.find(".moveUpButton").on("click", function(e){Identifiers.moveUp($(this))});
			addedToGenerate.find(".moveDownButton").on("click", function(e){Identifiers.moveDown($(this))});
			addedToGenerate.find(".removeButton").on("click", function(e){Identifiers.removeIdentifier($(this))});

			Identifiers.getIdentifiersContainer(Identifiers.getInputContainer(generateButtonJq)).append(addedToGenerate);
		},
	},

	searchSelectModal: $("#idGeneratorSearchSelectModal"),
	searchSelectContainer: $("#idGeneratorSelectSearchAddContainer"),
	associatedContainer: $("#idGeneratorSelectSearchFromAssociatedContainer"),
	searchSelectForm: $("#idGeneratorSearchSelectForm"),
	searchSelectSearchResults: $("#idGeneratorSearchSelectResults"),
	newGeneratorForm: $("#idGeneratorSearchSelectNewGeneratorForm"),
	associatedList: $("#idGeneratorSelectSearchFromAssociatedList"),

	setupSearchSelect: function (idGenSelectAddInputJq, forObject, genList = null) {
		console.log("Setting up id generator search select for: ", forObject);

		ModalUtils.setReturnModal(IdGeneratorSearchSelect.searchSelectModal, idGenSelectAddInputJq);

		IdGeneratorSearchSelect.searchSelectContainer.show();
		IdGeneratorSearchSelect.associatedContainer.hide();
		IdGeneratorSearchSelect.associatedList.text("");

		let destinationId = idGenSelectAddInputJq.attr("id");
		IdGeneratorAddEdit.setupFormForAdd(IdGeneratorSearchSelect.newGeneratorForm, false, true);
		IdGeneratorSearchSelect.searchSelectModal.data("destinationId", destinationId);

		if (forObject) {
			let forObjectInput = IdGeneratorSearchSelect.searchSelectForm.find("select[name='generatorFor']");
			forObjectInput.val(forObject);
			forObjectInput.find(':selected').prop('disabled', false);
			forObjectInput.find(':not(:selected)').prop('disabled', true);

			let addForObj = IdGeneratorAddEdit.formGetters.generatesFor(IdGeneratorSearchSelect.newGeneratorForm);
			addForObj.val(forObject);
			addForObj.find(':selected').prop('disabled', false);
			addForObj.find(':not(:selected)').prop('disabled', true);
			DselectUtils.setupDselect(addForObj[0]);
		}

		if(genList !== null){
			IdGeneratorSearchSelect.searchSelectContainer.hide();
			IdGeneratorSearchSelect.associatedContainer.show();

			for(let curGenId in genList){
				Getters.Identifiers.generator(genList[curGenId]).then(function (idGenData){
					if(forObject && !idGenData.forObjectType.includes(forObject)){
						return;
					}

					let newSelection = $(`
<div class="col-lg-3 col-md-4 col-sm-6 col-xs-12">
	<div class="card" >
		<div class="card-body">
			<h5 class="card-title"></h5>
			<p class="card-text formatContainer"></p>
			<div class="d-grid gap-2">
				<button class="btn btn-primary btn-sm selectIdGenButton" data-bs-target="#exampleModalToggle" data-bs-toggle="modal">Select</button>
			</div>
		</div>
	</div>
</div>
`);

					newSelection.on("click", function (e) {IdGeneratorSearchSelect.selectIdGenerator(idGenData)});
					newSelection.find(".card-title").text(idGenData.name);
					newSelection.find(".formatContainer").text(idGenData.idFormat);

					newSelection.find(".selectIdGenButton").attr("data-bs-target", "#" + IdGeneratorSearchSelect.searchSelectModal.data("bs-othermodalid"));

					IdGeneratorSearchSelect.associatedList.append(newSelection);
				});
			}
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
		} else if(destination.hasClass("idGeneratorAssociatedInput")){
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
