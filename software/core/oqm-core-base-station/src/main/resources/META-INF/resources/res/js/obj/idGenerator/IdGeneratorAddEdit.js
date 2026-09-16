import {DselectUtils} from "../../DselectUtils.js";
import {Rest} from "../../Rest.js";
import {PageMessageUtils} from "../../PageMessageUtils.js";
import {Getters} from "../Getters.js";
import {PageUtility} from "../../utilClasses/PageUtility.js";

export class IdGeneratorAddEdit extends PageUtility {
	static formGetters = class {
		static messages(formJq){
			return formJq.find("div.formMessages");
		}
		static id(formJq){
			return formJq.find("input[name='generatorId']");
		}
		static generatesFor(formJq){
			return formJq.find("select[name='generatesFor']");
		}
		static name(formJq){
			return formJq.find("input[name='name']");
		}
		static encoded(formJq){
			return formJq.find("input[name='encoded']");
		}
		static barcode(formJq){
			return formJq.find("input[name='barcode']");
		}
		static format(formJq){
			return formJq.find("input[name='format']");
		}
	}
	static forms = class {
		static modal = $("#idGeneratorAddEditForm")
	}

	static resetForm(formJq){
		console.log("Resetting unique id generator form.")
		formJq.trigger("reset");
		IdGeneratorAddEdit.formGetters.messages(formJq).text("");
		IdGeneratorAddEdit.formGetters.id(formJq).val("");
		IdGeneratorAddEdit.formGetters.name(formJq).val("");
		IdGeneratorAddEdit.formGetters.generatesFor(formJq).prop("disabled", false);
		IdGeneratorAddEdit.formGetters.generatesFor(formJq).find("option").prop("disabled", false).prop("checked", false);
		DselectUtils.resetDselect( IdGeneratorAddEdit.formGetters.generatesFor(formJq));
		IdGeneratorAddEdit.formGetters.barcode(formJq).prop("checked", false);
		IdGeneratorAddEdit.formGetters.encoded(formJq)
			.prop("checked", false)
			.prop("disabled", false);
		IdGeneratorAddEdit.formGetters.format(formJq).val("")
			.prop("disabled", false);
	}

	static setupFormForAdd(formJq, modalJq=false, select=false){
		IdGeneratorAddEdit.resetForm(formJq);

		if(select){
			formJq.data("select", true);
		}

		if(modalJq){
			modalJq.find(".modalTitleText").text("Add ID Generator");
		}
	}

	static setupFormForEdit(formJq, uniqueIdGeneratorId, modalJq=false){
		IdGeneratorAddEdit.resetForm(formJq);
		IdGeneratorAddEdit.formGetters.encoded(formJq)
			.prop("disabled", true);
		IdGeneratorAddEdit.formGetters.format(formJq).val("")
			.prop("disabled", true);

		if(modalJq){
			modalJq.find(".modalTitleText").text("Edit ID Generator");
		}
		//TODO:: change label to edit if modal

		Getters.Identifiers.generator(uniqueIdGeneratorId)
			.then(function(generator){
				IdGeneratorAddEdit.formGetters.id(formJq).val(generator.id);
				IdGeneratorAddEdit.formGetters.name(formJq).val(generator.name);

				IdGeneratorAddEdit.formGetters.generatesFor(formJq).prop("disabled", true);//TODO:: not working
				DselectUtils.setValues(IdGeneratorAddEdit.formGetters.generatesFor(formJq), generator.forObjectType);

				//TODO:: generates
				//TODO:: for
				IdGeneratorAddEdit.formGetters.barcode(formJq).prop("checked", generator.barcode);
				IdGeneratorAddEdit.formGetters.encoded(formJq).prop("checked", generator.encoded);
				IdGeneratorAddEdit.formGetters.format(formJq).val(generator.idFormat);
			});
	}

	static buildGeneratorObject(formJq){
		let generatorObj = {
			name: IdGeneratorAddEdit.formGetters.name(formJq).val(),
			forObjectType: IdGeneratorAddEdit.formGetters.generatesFor(formJq).val(),
			encoded: IdGeneratorAddEdit.formGetters.encoded(formJq).prop("checked"),
			barcode: IdGeneratorAddEdit.formGetters.barcode(formJq).prop("checked"),
			idFormat: IdGeneratorAddEdit.formGetters.format(formJq).val()
		}

		if(IdGeneratorAddEdit.formGetters.id(formJq).val() !== ""){
			generatorObj.id = IdGeneratorAddEdit.formGetters.id(formJq).val();
		}

		return generatorObj;
	}

	static submitAddEditForm(e, formJq, refreshOnSuccess = true){
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

					if(formJq.data("select")){
						let destinationId = formJq.data("destination");
						console.log("Sending back to destination: ", destinationId);
						IdGeneratorSearchSelect.selectIdGenerator(data);
						IdGeneratorSearchSelect.closeModal();
					} else {
						if(refreshOnSuccess){
							PageMessageUtils.reloadPageWithMessage("Successfully added new id generator.", "success");
						} else {
							PageMessageUtils.addMessageToDiv(IdGeneratorAddEdit.formGetters.messages(formJq), "success", "Successfully added new id generator.")
						}
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
						PageMessageUtils.reloadPageWithMessage("Successfully edited id generator.", "success");
					} else {
						PageMessageUtils.addMessageToDiv(IdGeneratorAddEdit.formGetters.messages(formJq), "success", "Successfully edited id generator.")
					}
				}
			});
		}
	}

	static setupFormSubmit(formJq, modalJq = false) {
		formJq.on("submit", function (event) {
			IdGeneratorAddEdit.submitAddEditForm(event, formJq);
		});
	}

	static {
		window.IdGeneratorAddEdit = this;

		$(".idGeneratorAddEditForm").each(function(i, formJs){
			IdGeneratorAddEdit.setupFormSubmit($(formJs));
		});
	}
}
