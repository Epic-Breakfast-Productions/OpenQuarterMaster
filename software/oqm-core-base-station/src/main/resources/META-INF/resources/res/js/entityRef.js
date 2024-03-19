
const EntityRef = {
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
	}
}