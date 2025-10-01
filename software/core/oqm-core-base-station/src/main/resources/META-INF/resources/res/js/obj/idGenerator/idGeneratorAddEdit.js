
const IdGeneratorAddEdit = {
	formGetters: {
		messages: function(formJq){
			return formJq.find("div.formMessages");
		},
		id: function(formJq){
			return formJq.find("input[name='generatorId']");
		},
		generates: function(formJq){
			return formJq.find("select[name='generates']");
		},
		generatesFor: function(formJq){
			return formJq.find("select[name='generatesFor']");
		},
		name: function(formJq){
			return formJq.find("input[name='name']");
		},
		encoded: function(formJq){
			return formJq.find("input[name='encoded']");
		},
		barcode: function(formJq){
			return formJq.find("input[name='barcode']");
		},
		format: function(formJq){
			return formJq.find("input[name='format']");
		}
	},
	forms: {
		modal: $("#idGeneratorAddEditForm")
	},

	resetForm(formJq){
		console.log("Resetting unique id generator form.")
		formJq.trigger("reset");
		IdGeneratorAddEdit.formGetters.messages(formJq).text("");
		IdGeneratorAddEdit.formGetters.id(formJq).val("");
		IdGeneratorAddEdit.formGetters.name(formJq).val("");
		IdGeneratorAddEdit.formGetters.generates(formJq).prop("disabled", false);
		IdGeneratorAddEdit.formGetters.generatesFor(formJq).prop("disabled", false);
		Dselect.resetDselect( IdGeneratorAddEdit.formGetters.generatesFor(formJq));
		IdGeneratorAddEdit.formGetters.barcode(formJq).prop("checked", false);
		IdGeneratorAddEdit.formGetters.encoded(formJq)
			.prop("checked", false)
			.prop("disabled", false);
		IdGeneratorAddEdit.formGetters.format(formJq).val("")
			.prop("disabled", false);
	},

	setupFormForAdd(formJq, modalJq=false){
		IdGeneratorAddEdit.resetForm(formJq);

		if(modalJq){
			modalJq.find(".modalTitleText").text("Add ID Generator");
		}
	},

	setupFormForEdit(formJq, uniqueIdGeneratorId, modalJq=false){
		IdGeneratorAddEdit.resetForm(formJq);
		IdGeneratorAddEdit.formGetters.encoded(formJq)
			.prop("disabled", true);
		IdGeneratorAddEdit.formGetters.format(formJq).val("")
			.prop("disabled", true);

		if(modalJq){
			modalJq.find(".modalTitleText").text("Edit ID Generator");
		}
		//TODO:: change label to edit if modal

		Getters.UniqueId.generator(uniqueIdGeneratorId)
			.then(function(generator){
				IdGeneratorAddEdit.formGetters.id(formJq).val(generator.id);
				IdGeneratorAddEdit.formGetters.name(formJq).val(generator.name);

				IdGeneratorAddEdit.formGetters.generates(formJq).val(generator.generates);
				IdGeneratorAddEdit.formGetters.generates(formJq).prop("disabled", true);
				IdGeneratorAddEdit.formGetters.generatesFor(formJq).prop("disabled", true);//TODO:: not working
				Dselect.setValues(IdGeneratorAddEdit.formGetters.generatesFor(formJq), generator.forObjectType);
				IdGeneratorAddEdit.formGetters.barcode(formJq).prop("checked", generator.barcode);
				IdGeneratorAddEdit.formGetters.encoded(formJq).prop("checked", generator.encoded);
				IdGeneratorAddEdit.formGetters.format(formJq).val(generator.idFormat);
			});
	},

	buildGeneratorObject(formJq){
		let generatorObj = {
			name: IdGeneratorAddEdit.formGetters.name(formJq).val(),
			generates: IdGeneratorAddEdit.formGetters.generates(formJq).val(),
			forObjectType: IdGeneratorAddEdit.formGetters.generatesFor(formJq).val(),
			encoded: IdGeneratorAddEdit.formGetters.encoded(formJq).prop("checked"),
			barcode: IdGeneratorAddEdit.formGetters.barcode(formJq).prop("checked"),
			idFormat: IdGeneratorAddEdit.formGetters.format(formJq).val()
		}

		if(IdGeneratorAddEdit.formGetters.id(formJq).val() !== ""){
			generatorObj.id = IdGeneratorAddEdit.formGetters.id(formJq).val();
		}

		return generatorObj;
	},

	submitAddEditForm(e, formJq, refreshOnSuccess = true){
		e.preventDefault();

		let generatorObj = IdGeneratorAddEdit.buildGeneratorObject(formJq);

		if(!generatorObj.id){
			Rest.call({
				spinnerContainer: formJq[0],
				method: "post",
				url: Rest.passRoot + "/identifier/generator",
				data: generatorObj,
				failMessagesDiv: IdGeneratorAddEdit.formGetters.messages(formJq),
				done: function (data) {
					if(refreshOnSuccess){
						PageMessages.reloadPageWithMessage("Successfully added new id generator.", "success");
					} else {
						PageMessages.addMessageToDiv(IdGeneratorAddEdit.formGetters.messages(formJq), "success", "Successfully added new id generator.")
					}
				}
			});
		} else {
			Rest.call({
				spinnerContainer: formJq[0],
				method: "put",
				url: Rest.passRoot + "/identifier/generator/" + generatorObj.id,
				data: generatorObj,
				failMessagesDiv: IdGeneratorAddEdit.formGetters.messages(formJq),
				done: function (data) {
					if(refreshOnSuccess){
						PageMessages.reloadPageWithMessage("Successfully edited id generator.", "success");
					} else {
						PageMessages.addMessageToDiv(IdGeneratorAddEdit.formGetters.messages(formJq), "success", "Successfully edited id generator.")
					}
				}
			});
		}
	},

	setupFormSubmit(formJq, modalJq = false){
		formJq.on("submit", function(event){
			IdGeneratorAddEdit.submitAddEditForm(event, formJq);
		});
	}
}

if(IdGeneratorAddEdit.forms.modal){
	IdGeneratorAddEdit.setupFormSubmit(IdGeneratorAddEdit.forms.modal);
}
