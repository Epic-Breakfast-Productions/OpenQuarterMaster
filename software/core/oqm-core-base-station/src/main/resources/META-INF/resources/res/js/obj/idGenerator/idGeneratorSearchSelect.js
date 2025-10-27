const IdGeneratorSearchSelect = {
	IdGenInput: {
		getInputContainer(innerElementJq) {
			return innerElementJq.closest(".idGeneratorInput");
		},
		getAssociatedGeneratorList(idGenInputJq) {
			return idGenInputJq.find(".associatedIdentifierGeneratorTableContent");
		},
		init(idGenInputJq) {

		},
		associateButtonClicked: function (buttonJs) {
			console.debug("Clicked associate button.");
			let buttonJq = $(buttonJs);
			ModalHelpers.setReturnModal($("#idGeneratorSearchSelectModal"), buttonJq);

			let idGenInputContainer = IdGeneratorSearchSelect.IdGenInput.getInputContainer(buttonJq);

			IdGeneratorSearchSelect.setupSearchSelect(
				idGenInputContainer,
				idGenInputContainer.data("generates"),
				idGenInputContainer.data("forobject")
			);
		},
		associateIdGenerator: function (idGenInputJq, idGenData) {
			if (idGenInputJq instanceof String) {
				//TODO:: get, re-call
			}
			if (idGenData instanceof String) {
				//TODO:: get, re-call
			}

			//TODO:: skip if not proper type of id generator

			let newRow = $('<tr class="associatedIdentifierGenerator"></tr>');

			newRow.append($('<td class="idGeneratorName"></td>').text(idGenData.name));

			newRow.append(
				$('<td></td>')
					.append(
						$('<button type="button" class="btn btn-sm btn-outline-success me-1" onclick="IdGeneratorSearchSelect.IdGenInput.addIdToGenerate($(this))" title="Use This ID Generator to make a new ID"></button>')
							.html(Icons.add)
					)
					.append( //TODO:: don't do this if no associate button
						$('<button type="button" class="btn btn-sm btn-outline-danger" onclick="$(this).closest(\'.associatedIdentifierGenerator\').remove()" title="Unassociate this ID generator"></button>')
							.html(Icons.remove)
					)
			);


			IdGeneratorSearchSelect.IdGenInput.getAssociatedGeneratorList(idGenInputJq).append(newRow);
		},
		addIdToGenerate(buttonJq) {
			//TODO
		}
	},

	searchSelectModal: $("#idGeneratorSearchSelectModal"),
	searchSelectForm: $("#idGeneratorSearchSelectForm"),
	searchSelectSearchResults: $("#idGeneratorSearchSelectResults"),
	newGeneratorForm: $("#idGeneratorSearchSelectNewGeneratorForm"),

	setupSearchSelect: function (idGenSelectAddInputJq, generates, forObject) {
		console.log("Setting up search select for: ", generates, forObject);
		IdGeneratorAddEdit.setupFormForAdd(IdGeneratorSearchSelect.newGeneratorForm, false, idGenSelectAddInputJq.attr("id"));


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


	addUniqueId: function () {

	},
	setupModal: function () {

	}
}

//TODO:: setup search for search select

IdGeneratorSearchSelect.searchSelectForm.on("submit", function (event) {
	event.preventDefault();
	console.log("Submitting search form.");

	var searchParams = new URLSearchParams(new FormData(event.target));
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
			"inputIdPrepend": IdGeneratorSearchSelect.searchSelectModal.attr("data-bs-inputIdPrepend")
			// ,
			// "otherModalId": IdGeneratorSearchSelect.itemSearchSelectModal.attr("data-bs-otherModalId")
		},
		async: false,
		done: function (data) {
			console.log("Got data!");
			IdGeneratorSearchSelect.searchSelectSearchResults.html(data);
		}
	});
});
