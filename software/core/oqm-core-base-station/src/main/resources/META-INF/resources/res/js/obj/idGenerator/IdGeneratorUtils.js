import {Rest} from "../../Rest.js";
import {PageUtility} from "../../utilClasses/PageUtility.js";

export class IdGeneratorUtils extends PageUtility {
	static modalResultContainer = $("#idGeneratorGenerateResultModalContainer");

	static async generate(generatorId, n = 1, resultContainerJq = null, doneFunc = function(){}) {
		console.log("Generating ids for generator: ", generatorId);
		let output = null;

		await Rest.call({
			method: "GET",
			url: Rest.passRoot + "/identifier/generator/" + generatorId + "/generate",
			done: function(result){
				output = result;

				if(resultContainerJq != null){
					resultContainerJq.text("");

					let list = $("<ul></ul>");
					result["generatedIds"].forEach(function (newId){
						let newLi = $("<li></li>");
						newLi.text(newId.value);
						list.append(newLi);
					});
					resultContainerJq.append(list);
				}

				doneFunc(result);
			}
		});
		return output;
	}

	static {
		window.IdGeneratorUtils = this;
	}
}