import {Rest} from "./Rest.js";
import {Icons} from "./Icons.js";

export const EntityRef = {
	getEntityRef(entityId, doneFunc){
		return Rest.call({
			spinnerContainer: null,
			url: Rest.passRoot + "/interacting-entity/"+entityId+"/reference",
			method: "GET",
			extraHeaders: {
				"accept": "text/html"
			},
			returnType: "html",
			done: function (data) {
				doneFunc(data);
			}
		});
	},
	getEntityRefIcon(entityOrType){
		if (typeof entityOrType === 'object' && entityOrType !== null) {
			console.log("Entity is object: ", entityOrType);
			entityOrType = entityOrType.type;
		}

		console.log("Getting symbol for entity type: ", entityOrType);

		let output = "";
		switch(entityOrType){
			case "USER":
				output = Icons.user;
				break;
			case "SERVICE_GENERAL":
				output = Icons.extService;
				break;
			case "CORE_API":
				output = Icons.coreApi;
				break;
		}

		return $(output);
	}
}