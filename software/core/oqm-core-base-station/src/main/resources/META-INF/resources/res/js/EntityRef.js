import {Rest} from "./Rest.js";
import {Icons} from "./Icons.js";
import {PageUtility} from "./utilClasses/PageUtility.js";

export class EntityRef extends PageUtility {
	static getEntityRef(entityId, doneFunc){
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
	}
	static getEntityRefIcon(entityOrType){
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
	static {
		window.EntityRef = this;
	}
}