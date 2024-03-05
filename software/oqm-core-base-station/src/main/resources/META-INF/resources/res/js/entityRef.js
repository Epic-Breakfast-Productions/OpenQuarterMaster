
const EntityRef = {
	getEntityRef(entityRef, doneFunc){
		return Rest.call({
			spinnerContainer: null,
			url: "/api/v1/interacting-entity/reference",
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