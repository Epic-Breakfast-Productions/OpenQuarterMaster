
const EntityRef = {
	getEntityRef(entityRef, doneFunc){
		return Rest.call({
			spinnerContainer: null,
			url: Rest.passRoot + "/interacting-entity/reference",
			method: "POST",
			data: entityRef,
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