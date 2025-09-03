
const GeneralIdentifiers = {
	getInputContainer(subElementJq){
		return subElementJq.closest('.generalIdInputContainer');
	},
	getIdentifiersContainer(generalInputContainerJq){
		return generalInputContainerJq.find(".identifiersContainer");
	},
	getMessagesContainer(generalInputContainerJq){
		return generalInputContainerJq.find(".identifierMessagesContainer");
	},
	getNewIdentifierInput(generalInputContainerJq){
		return generalInputContainerJq.find("input[name='newIdentifier']");
	},
	getIdentifierContainer(subElementJq){
		return subElementJq.closest('.generalIdentifierContainer');
	},
	getIdentifierImage(idContainerJq){
		return idContainerJq.find(".identifierImage");
	},
	getIdentifierValueContainer(idContainerJq){
		return idContainerJq.find(".identifierValue");
	},
	getIdentifierValue(idContainerJq){
		return GeneralIdentifiers.getIdentifierValueContainer(idContainerJq).text();
	},
	getIdentifierType(idContainerJq){
		return idContainerJq.find(".identifierType").text();
	},
	getIdentifierKey(idContainerJq){
		return idContainerJq.find("input[name='generalIdKey']").val();
	},
	getIdentifierIsBarcodeCheckbox(idContainerJq){
		return idContainerJq.find("input[name='generalIdIsBarcode']");
	},
	getNewIdentifierValue(generalInputContainerJq){
		return GeneralIdentifiers.getNewIdentifierInput(generalInputContainerJq).val();
	},
	clearInput(generalInputContainerJq){
		GeneralIdentifiers.getNewIdentifierInput(generalInputContainerJq).val("");
	},
	reset(generalInputContainerJq){
		GeneralIdentifiers.clearInput(generalInputContainerJq);
		GeneralIdentifiers.getIdentifiersContainer(generalInputContainerJq).html("");
	},
	addIdentifier(generalInputContainerJq){
		let newIdentifier = GeneralIdentifiers.getNewIdentifierValue(generalInputContainerJq);
		console.log("Adding a new general identifier: ", newIdentifier);

		return Rest.call({
			failMessagesDiv: generalInputContainerJq,
			url: Rest.passRoot + "/identifier/general/getIdObject/" + newIdentifier,
			returnType: "html",
			extraHeaders: {
				"accept": "text/html",
			},
			done: function (data) {
				GeneralIdentifiers.getIdentifiersContainer(generalInputContainerJq).append(data);
				GeneralIdentifiers.clearInput(generalInputContainerJq);
			}
		});
	},
	handleIsBarcodeChecked(isBarcodeCheckboxJq){
		let idContainerJq = GeneralIdentifiers.getIdentifierContainer(isBarcodeCheckboxJq);
		let isChecked = isBarcodeCheckboxJq.prop("checked");

		let identifierValueContainer = GeneralIdentifiers.getIdentifierValueContainer(idContainerJq);
		let identifierImage = GeneralIdentifiers.getIdentifierImage(idContainerJq);

		if(isChecked){
			identifierImage.removeClass("d-none");
			identifierValueContainer.addClass("d-none");
		} else {
			identifierValueContainer.removeClass("d-none");
			identifierImage.addClass("d-none");
		}
	},
	moveUp(upButtonJq){
		SelectedObjectDivUtils.moveUp(GeneralIdentifiers.getIdentifierContainer(upButtonJq));
	},
	moveDown(downButtonJq){
		SelectedObjectDivUtils.moveDown(downButtonJq.closest('.generalIdentifierContainer'));
	},
	removeIdentifier(removeButtonJq){
		if(confirm("Are you sure you want to remove this identifier?") === false) return;
		SelectedObjectDivUtils.removeSelected(removeButtonJq.closest('.generalIdentifierContainer'));
	},
	getGeneralIdData(generalInputContainerJq){
		let getIdentifiersContainer = GeneralIdentifiers.getIdentifiersContainer(generalInputContainerJq);
		let output = {};

		getIdentifiersContainer.find(".generalIdentifierContainer").each(function (i, curIdContainer){
			let curIdContainerJq = $(curIdContainer);
			let curIdObj = {
				value:  GeneralIdentifiers.getIdentifierValue(curIdContainerJq),
				type: GeneralIdentifiers.getIdentifierType(curIdContainerJq)
			}

			if(curIdObj.type === "GENERIC"){
				curIdObj["isBarcode"] = GeneralIdentifiers.getIdentifierIsBarcodeCheckbox(curIdContainerJq).prop("checked")
			}

			//TODO:: not getting hey
			output[GeneralIdentifiers.getIdentifierKey(curIdContainerJq)] = curIdObj;
		});

		return output;
	}
}