import {Rest} from "./Rest.js";
import {SelectedObjectDivUtils} from "./SelectedObjectDivUtils.js";
import {PageComponents} from "./PageComponents.js";

export class Identifiers {
	static getInputContainer(subElementJq) {
		return subElementJq.closest('.identifierInputContainer');
	}
	static getIdentifiersContainer(identifierInputContainerJq) {
		return identifierInputContainerJq.find(".identifiersContainer");
	}
	static getMessagesContainer(identifierInputContainerJq) {
		return identifierInputContainerJq.find(".identifierMessagesContainer");
	}
	static getAssociateButton(identifierInputContainerJq) {
		return identifierInputContainerJq.find(".idGeneratorGenerateButton");
	}
	static getNewIdentifierInput(identifierInputContainerJq) {
		return identifierInputContainerJq.find("input[name='newIdentifier']");
	}
	static getNewIdentifierValue(identifierInputContainerJq) {
		return Identifiers.getNewIdentifierInput(identifierInputContainerJq).val();
	}
	static getIdentifierContainer(subElementJq) {
		return subElementJq.closest('.addedIdentifierContainer');
	}
	static getIdentifierImage(idContainerJq) {
		return idContainerJq.find(".identifierImage");
	}
	static getIdentifierValueContainer(idContainerJq) {
		return idContainerJq.find(".identifierValue");
	}
	static getIdentifierValue(idContainerJq) {
		return Identifiers.getIdentifierValueContainer(idContainerJq).text();
	}
	static getIdentifierTypeContainer(idContainerJq) {
		return idContainerJq.find(".identifierType");
	}
	static getIdentifierType(idContainerJq) {
		return Identifiers.getIdentifierTypeContainer(idContainerJq).text();
	}
	static getIdentifierLabelInput(idContainerJq) {
		return idContainerJq.find("input[name='label']");
	}
	static getIdentifierLabel(idContainerJq) {
		return Identifiers.getIdentifierLabelInput(idContainerJq).val();
	}
	static getIdentifierIsBarcodeCheckbox(idContainerJq) {
		return idContainerJq.find("input[name='identifierIsBarcode']");
	}
	static clearInput(identifierInputContainerJq) {
		Identifiers.getNewIdentifierInput(identifierInputContainerJq).val("");
	}
	static reset(identifierInputContainerJq) {
		Identifiers.clearInput(identifierInputContainerJq);
		Identifiers.getIdentifiersContainer(identifierInputContainerJq).html("");
	}
	static addIdentifierFromValue(identifierInputContainerJq, newIdentifier){
		console.log("Adding a new identifier: ", newIdentifier);

		return Rest.call({
			failMessagesDiv: identifierInputContainerJq,
			url: Rest.passRoot + "/identifier/getIdObject/" + newIdentifier,
			returnType: "html",
			extraHeaders: {
				"accept": "text/html",
			},
			done: function (data) {
				Identifiers.getIdentifiersContainer(identifierInputContainerJq).append(data);
				Identifiers.clearInput(identifierInputContainerJq);
			}
		});
	}
	static addIdentifier(identifierInputContainerJq) {
		let newIdentifier = Identifiers.getNewIdentifierValue(identifierInputContainerJq);
		if(newIdentifier === "") {
			console.log("Not adding empty identifier.");
			return;
		}

		return Identifiers.addIdentifierFromValue(identifierInputContainerJq, newIdentifier);
	}
	static addToGenerate(generateIdButtonJq, generatorData) {
		let idContainer = Identifiers.getIdentifiersContainer(Identifiers.getInputContainer(generateIdButtonJq));
	}
	static handleIsBarcodeNeedUpdate(inputJq) {
		console.debug("Handling barcode checkbox change.");
		let idContainerJq = Identifiers.getIdentifierContainer(inputJq);
		let isBarcodeCheckboxJq = Identifiers.getIdentifierIsBarcodeCheckbox(idContainerJq);
		let identifierValueContainer = Identifiers.getIdentifierValueContainer(idContainerJq);
		let identifierImage = Identifiers.getIdentifierImage(idContainerJq);

		let type = Identifiers.getIdentifierType(idContainerJq);
		let isChecked = isBarcodeCheckboxJq.prop("checked");
		let labelVal = Identifiers.getIdentifierLabel(idContainerJq);

		if(labelVal == ""){
			labelVal = type;
		}

		if (isChecked) {
			identifierImage.removeClass("d-none");
			identifierValueContainer.addClass("d-none");

			let imgSrc = Rest.passRoot +
				"/identifier/barcode/" +
				encodeURIComponent(type) + "/" +
				encodeURIComponent(Identifiers.getIdentifierValue(idContainerJq)) + "/" +
				encodeURIComponent(labelVal);

			if (identifierImage.attr("src") !== imgSrc) {
				console.debug("Updating barcode url: ", imgSrc);
				identifierImage.attr("src", imgSrc);
			}
		} else {
			identifierValueContainer.removeClass("d-none");
			identifierImage.addClass("d-none");
		}
	}
	static moveUp(upButtonJq) {
		SelectedObjectDivUtils.moveUp(Identifiers.getIdentifierContainer(upButtonJq));
	}
	static moveDown(downButtonJq) {
		SelectedObjectDivUtils.moveDown(Identifiers.getIdentifierContainer(downButtonJq));
	}
	static removeIdentifier(removeButtonJq) {
		if (confirm("Are you sure you want to remove this identifier?") === false) return;
		SelectedObjectDivUtils.removeSelected(Identifiers.getIdentifierContainer(removeButtonJq));
	}
	static getIdentifierData(identifierInputContainerJq) {
		let getIdentifiersContainer = Identifiers.getIdentifiersContainer(identifierInputContainerJq);
		let output = [];

		getIdentifiersContainer.find(".addedIdentifierContainer").each(function (i, curIdContainer) {
			let curIdContainerJq = $(curIdContainer);
			let curIdObj = {
				label: Identifiers.getIdentifierLabel(curIdContainerJq)
			};

			if (curIdContainerJq.hasClass("toGenerateContainer")) {
				curIdObj['type'] = "TO_GENERATE";
				curIdObj['generateFrom'] = curIdContainerJq.find(".fromGenerator").data("generator");
			} else {
				curIdObj['value'] = Identifiers.getIdentifierValue(curIdContainerJq);
				curIdObj['type'] = Identifiers.getIdentifierType(curIdContainerJq);
				curIdObj['label'] = Identifiers.getIdentifierLabel(curIdContainerJq);

				if (curIdObj.type === "GENERIC") {
					curIdObj["barcode"] = Identifiers.getIdentifierIsBarcodeCheckbox(curIdContainerJq).prop("checked")
				}
			}

			output.push(curIdObj);
		});

		console.log("Identifiers gathered: ", output);

		return output;
	}
	static newAddedIdentifier(identifier) {
		let idInput = $(PageComponents.Inputs.Identifiers.identifierAdded);

		Identifiers.getIdentifierValueContainer(idInput).text(identifier.value);
		Identifiers.getIdentifierTypeContainer(idInput).text(identifier.type);
		Identifiers.getIdentifierLabelInput(idInput).val(identifier.label);

		if (identifier.barcode) {
			let barcodeImage = idInput.find(".identifierImage");

			barcodeImage.attr("src", Rest.passRoot + "/identifier/barcode/" + encodeURIComponent(identifier.type) + "/" + encodeURIComponent(identifier.value) + "/" + encodeURIComponent(identifier.label));
			barcodeImage.removeClass("d-none");
		} else {
			idInput.find(".identifierValue").removeClass("d-none");
		}

		Identifiers.getIdentifierIsBarcodeCheckbox(idInput).prop("checked", identifier.barcode);
		if (identifier.type !== "GENERIC") {
			idInput.find(".identifierIsBarcodeSelectContainer").addClass("d-none");
		}

		return idInput;
	}

	static populateEdit(identifierInputContainerJq, identifierList) {
		let getIdentifiersContainer = Identifiers.getIdentifiersContainer(identifierInputContainerJq);
		for (const identifier of identifierList) {
			let idInput = Identifiers.newAddedIdentifier(identifier);

			getIdentifiersContainer.append(idInput);
		}
	}
	static setupForAssociated(identifierInputContainerJq, identifierList){
		Identifiers.getAssociateButton(identifierInputContainerJq).data("idGeneratorList", identifierList);
	}
	static View = class {
		static showInDiv(divJq, identifierArray) {
			for (const identifier of identifierArray) {
				let newIdShow = $(`
<div class="col-sm-6 col-md-6 col-lg-4 mb-1 identifierContainer">
	<div class="card identifierDisplay">
		<div class="card-header p-1 text-center">
			<h5 class="card-title mb-0 identifierKey user-select-all"></h5>
		</div>
		<a href="" target="_blank" class="identifierImageLink d-none">
			<img src="" class="card-img identifierImage" alt="Barcode Image">
		</a>
		<div class="card-body p-1">
			<div class="identifierValueContainer text-center">
				<p class="h4 card-subtitle identifierValue user-select-all mb-0"></p>
				<p class="text-secondary mb-1">
					<small class="identifierType"></small>
					` + PageComponents.Inputs.copyButton + `
				</p>
			</div>
		</div>
	</div>
</div>`);
				let valueDiv = newIdShow.find(".identifierValue");
				let imageLink = newIdShow.find(".identifierImageLink");

				newIdShow.find(".identifierKey").text(identifier.label);
				valueDiv.text(identifier.value);
				newIdShow.find(".identifierType").text(identifier.type);
				newIdShow.find(".copyTextButton").attr("onClick", "TextCopyUtils.copyText(this,$(this.parentElement.previousElementSibling));");

				if (identifier.barcode) {
					let barcodeUrl = Rest.passRoot + "/identifier/barcode/" + encodeURIComponent(identifier.type) + "/" + encodeURIComponent(identifier.value) + "/" + encodeURIComponent(identifier.label);
					valueDiv.addClass("d-none");
					imageLink.removeClass("d-none");
					imageLink.attr("href", barcodeUrl);
					newIdShow.find(".identifierImage").attr("src", barcodeUrl);
				}

				divJq.append(newIdShow);
			}
		}
	}
	static {
		window.Identifiers = Identifiers;
	}
}
