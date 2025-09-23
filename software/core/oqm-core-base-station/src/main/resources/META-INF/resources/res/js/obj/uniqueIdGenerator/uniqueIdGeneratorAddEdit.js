
const UniqueIdGeneratorAddEdit = {
	formGetters: {
		messages: function(formJq){
			return formJq.find("div.formMessages");
		},
		id: function(formJq){
			return formJq.find("input[name='generatorId']");
		},
		name: function(formJq){
			return formJq.find("input[name='name']");
		},
		encoded: function(formJq){
			return formJq.find("input[name='encoded']");
		},
		format: function(formJq){
			return formJq.find("input[name='format']");
		}
	},
	forms: {
		modal: $("#uniqueIdGeneratorAddEditForm")
	},

	resetForm(formJq){
		console.log("Resetting unique id generator form.")
		UniqueIdGeneratorAddEdit.formGetters.messages(formJq).text("");
		UniqueIdGeneratorAddEdit.formGetters.id(formJq).val("");
		UniqueIdGeneratorAddEdit.formGetters.name(formJq).val("");
		UniqueIdGeneratorAddEdit.formGetters.encoded(formJq)
			.prop("checked", false)
			.prop("disabled", false);
		UniqueIdGeneratorAddEdit.formGetters.format(formJq).val("")
			.prop("disabled", false);
	},

	setupFormForAdd(formJq, modalJq=false){
		UniqueIdGeneratorAddEdit.resetForm(formJq);

		if(modalJq){
			modalJq.find(".modalTitleText").text("Add Unique ID Generator");
		}
	},

	setupFormForEdit(formJq, uniqueIdGeneratorId, modalJq=false){
		UniqueIdGeneratorAddEdit.resetForm(formJq);
		UniqueIdGeneratorAddEdit.formGetters.encoded(formJq)
			.prop("disabled", true);
		UniqueIdGeneratorAddEdit.formGetters.format(formJq).val("")
			.prop("disabled", true);

		if(modalJq){
			modalJq.find(".modalTitleText").text("Edit Unique ID Generator");
		}
		//TODO:: change label to edit if modal

		//TODO:: load uniqueIDGen, populate
	},

	buildGeneratorObject(formJq){
		let generatorObj = {
			name: UniqueIdGeneratorAddEdit.formGetters.name(formJq).val(),
			encoded: UniqueIdGeneratorAddEdit.formGetters.encoded(formJq).prop("checked"),
			idFormat: UniqueIdGeneratorAddEdit.formGetters.format(formJq).val()
		}

		if(UniqueIdGeneratorAddEdit.formGetters.id(formJq).val() !== ""){
			generatorObj.id = UniqueIdGeneratorAddEdit.formGetters.id(formJq).val();
		}

		return generatorObj;
	},

	submitAddEditForm(e, formJq, refreshOnSuccess = true){
		e.preventDefault();

		let generatorObj = UniqueIdGeneratorAddEdit.buildGeneratorObject(formJq);

		if(!generatorObj.id){
			Rest.call({
				spinnerContainer: formJq[0],
				method: "post",
				url: Rest.passRoot + "/identifier/unique/generator",
				data: generatorObj,
				failMessagesDiv: UniqueIdGeneratorAddEdit.formGetters.messages(formJq),
				done: function (data) {
					if(refreshOnSuccess){
						PageMessages.reloadPageWithMessage("Successfully added new unique id generator.", "success");
					} else {
						PageMessages.addMessageToDiv(UniqueIdGeneratorAddEdit.formGetters.messages(formJq))
					}
				}
			});
		}
	},

	setupFormSubmit(formJq, modalJq = false){
		formJq.on("submit", function(event){
			UniqueIdGeneratorAddEdit.submitAddEditForm(event, formJq);
		});
	}
}

if(UniqueIdGeneratorAddEdit.forms.modal){
	UniqueIdGeneratorAddEdit.setupFormSubmit(UniqueIdGeneratorAddEdit.forms.modal);
}
