
const UniqueIdGeneratorUtils = {
	generate: async function(generatorId, n = 1, doneFunc = function(){}) {
		let output = null;

		await Rest.call({
			method: "GET",
			url: Rest.passRoot + "/identifier/unique/generator/" + generatorId + "/generate",
			done: function(result){
				output = result
				doneFunc(result);
			}
		});
		return output;
	}
}