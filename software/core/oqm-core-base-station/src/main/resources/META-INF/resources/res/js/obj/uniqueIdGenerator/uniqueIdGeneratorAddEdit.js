
const UniqueIdGeneratorAddEdit = {
	formGetters: {
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

	resetForm(formJq){
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
			format: UniqueIdGeneratorAddEdit.formGetters.format(formJq).val()
		}

		if(UniqueIdGeneratorAddEdit.formGetters.id(formJq).val() !== ""){
			generatorObj.id = UniqueIdGeneratorAddEdit.formGetters.id(formJq).val();
		}

		return generatorObj;
	},

	submitAddEditForm(formJq){
		UniqueIdGeneratorAddEdit.resetForm(formJq);

		let generatorObj = UniqueIdGeneratorAddEdit.buildGeneratorObject(formJq);
		//TODO:: submit to passthrough appropriately
	},

	setupFormSubmit(formJq, modalJq = false){
		formJq.on("submit", function(event){
			UniqueIdGeneratorAddEdit.submitAddEditForm(formJq);
		});
	}
}

//TODO:: setup modal form


