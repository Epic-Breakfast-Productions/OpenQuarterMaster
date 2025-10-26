const IdGeneratorSearchSelect = {
	IdGenInput: {
		getInputContainer(innerElementJq) {
			return innerElementJq.closest(".idGeneratorInput");
		},
		init(idGenInputJq) {

		},
		associateButtonClicked: function (buttonJs) {
			console.debug("Clicked associate button.");
			let buttonJq = $(buttonJs);
			ModalHelpers.setReturnModal($("#idGeneratorSearchSelectModal"), buttonJq);

			let idGenInputContainer = IdGeneratorSearchSelect.IdGenInput.getInputContainer(buttonJq);

			IdGeneratorSearchSelect.setupSearchSelect(
				idGenInputContainer.data("generates"),
				idGenInputContainer.data("forobject")
			);
		}
	},

	searchSelectModal: $("#idGeneratorSearchSelectModal"),
	searchSelectForm: $("#idGeneratorSearchSelectForm"),
	searchSelectSearchResults: $("#idGeneratorSearchSelectResults"),
	newGeneratorForm: $("#idGeneratorSearchSelectNewGeneratorForm"),

	setupSearchSelect: function (generates, forObject) {
		console.log("Setting up search select for: ", generates, forObject);
		IdGeneratorAddEdit.setupFormForAdd(IdGeneratorSearchSelect.newGeneratorForm);
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
		returnType:"html",
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
