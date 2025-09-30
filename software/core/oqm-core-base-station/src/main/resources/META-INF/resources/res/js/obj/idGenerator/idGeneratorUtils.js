
const IdGeneratorUtils = {
	modalResultContainer: $("#idGeneratorGenerateResultModalContainer"),

	generate: async function(generatorId, n = 1, resultContainerJq = null, doneFunc = function(){}) {
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
}