
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
	getIdentifierTypeContainer(idContainerJq){
		return idContainerJq.find(".identifierType");
	},
	getIdentifierType(idContainerJq){
		return GeneralIdentifiers.getIdentifierTypeContainer(idContainerJq).text();
	},
	getIdentifierLabelInput(idContainerJq){
		return idContainerJq.find("input[name='generalIdKey']");
	},
	getIdentifierLabel(idContainerJq){
		return GeneralIdentifiers.getIdentifierLabelInput(idContainerJq).val();
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
			if(identifierImage.attr("src") === ""){
				identifierImage.attr("src", Rest.passRoot + "/identifier/general/barcode/" + GeneralIdentifiers.getIdentifierType(idContainerJq) + "/" + GeneralIdentifiers.getIdentifierValue(idContainerJq));
			}
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
		let output = [];

		getIdentifiersContainer.find(".generalIdentifierContainer").each(function (i, curIdContainer){
			let curIdContainerJq = $(curIdContainer);
			let curIdObj = {
				value:  GeneralIdentifiers.getIdentifierValue(curIdContainerJq),
				type: GeneralIdentifiers.getIdentifierType(curIdContainerJq),
				label: GeneralIdentifiers.getIdentifierLabel(curIdContainerJq)
			}

			if(curIdObj.type === "GENERIC"){
				curIdObj["barcode"] = GeneralIdentifiers.getIdentifierIsBarcodeCheckbox(curIdContainerJq).prop("checked")
			}

			output.push(curIdObj);
		});

		return output;
	},
	populateEdit: function(generalInputContainerJq, generalIdentifierList){
		let getIdentifiersContainer = GeneralIdentifiers.getIdentifiersContainer(generalInputContainerJq);
		for (const generalIdentifier of generalIdentifierList) {
			let idInput = $(PageComponents.Inputs.GeneralIds.generalIdAdded);

			GeneralIdentifiers.getIdentifierValueContainer(idInput).text(generalIdentifier.value);
			GeneralIdentifiers.getIdentifierTypeContainer(idInput).text(generalIdentifier.type);
			GeneralIdentifiers.getIdentifierLabelInput(idInput).val(generalIdentifier.label);

			if(generalIdentifier.barcode){
				let barcodeImage = idInput.find(".identifierImage");

				barcodeImage.attr("src", Rest.passRoot + "/identifier/general/barcode/" + generalIdentifier.type + "/" + generalIdentifier.value);
				barcodeImage.removeClass("d-none");
			} else {
				idInput.find(".identifierValue").removeClass("d-none");
			}

			GeneralIdentifiers.getIdentifierIsBarcodeCheckbox(idInput).prop("checked", generalIdentifier.barcode);
			if(generalIdentifier.type !== "GENERIC"){
				idInput.find(".generalIdIsBarcodeSelectContainer").addClass("d-none");
			}

			getIdentifiersContainer.append(idInput);
		}
	},
	View: {
		showInDiv(divJq, generalIdentifierArray){
			for (const generalIdentifier of generalIdentifierArray) {
				let newIdShow = $(`
<div class="col-sm-6 col-md-6 col-lg-4 mb-1 generalIdentifierContainer">
	<div class="card identifierDisplay">
		<div class="card-header p-1 text-center">
			<h5 class="card-title mb-0 identifierKey"></h5>
		</div>
		<a href="" target="_blank" class="identifierImageLink d-none">
			<img src="" class="card-img identifierImage" alt="Barcode Image">
		</a>
		<div class="card-body p-1">
			<div class="identifierValueContainer text-center">
				<p class="h4 card-subtitle identifierValue text-nowrap user-select-all mb-0"></p>
				<p class="text-secondary mb-1">
					<small class="identifierType"></small>
					`+PageComponents.Inputs.copyButton+`
				</p>
			</div>
		</div>
	</div>
</div>`);
				let valueDiv = newIdShow.find(".identifierValue");
				let imageLink = newIdShow.find(".identifierImageLink");

				newIdShow.find(".identifierKey").text(generalIdentifier.label);
				valueDiv.text(generalIdentifier.value);
				newIdShow.find(".identifierType").text(generalIdentifier.type);
				newIdShow.find(".copyTextButton").attr("onClick", "TextCopyUtils.copyText(this,$(this.parentElement.previousElementSibling));");

				if(generalIdentifier.barcode){
					let barcodeUrl = Rest.passRoot + "/identifier/general/barcode/" + generalIdentifier.type + "/" + generalIdentifier.value;
					valueDiv.addClass("d-none");
					imageLink.removeClass("d-none");
					imageLink.attr("href", barcodeUrl);
					newIdShow.find(".identifierImage").attr("src", barcodeUrl);
				}

				divJq.append(newIdShow);
			}
		}
	}
}