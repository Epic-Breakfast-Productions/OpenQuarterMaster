
const GeneralIdentifiers = {
	getIdentifiersContainer(generalInputContainerJq){
		return generalInputContainerJq.find(".identifiersContainer");
	},
	getMessagesContainer(generalInputContainerJq){
		return generalInputContainerJq.find(".identifierMessagesContainer");
	},
	getNewIdentifierInput(generalInputContainerJq){
		return generalInputContainerJq.find("input[name='newIdentifier']");
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

		//TODO:: call api, add div with thing
	},
	moveUp(upButtonJq){
		SelectedObjectDivUtils.moveUp(upButtonJq.closest('.generalIdentifierContainer'));
	},
	moveDown(downButtonJq){
		SelectedObjectDivUtils.moveDown(downButtonJq.closest('.generalIdentifierContainer'));
	},
	removeIdentifier(removeButtonJq){
		if(confirm("Are you sure you want to remove this identifier?") === false) return;
		SelectedObjectDivUtils.removeSelected(removeButtonJq.closest('.generalIdentifierContainer'));
	}
}