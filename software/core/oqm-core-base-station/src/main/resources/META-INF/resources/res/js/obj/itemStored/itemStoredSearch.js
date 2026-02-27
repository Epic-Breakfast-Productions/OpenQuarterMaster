ItemStoredSearch = {

	search: async function (
		searchFormJs,
		event,
		resultsContainerSelector,
		showItem = false,
		showStorage = false,
		forSelect = false,
		selectDestination = ""
	) {
		event.preventDefault();
		console.log("Searching for items stored.");
		let searchFormJq = $(searchFormJs);
		let resultsContainer = $(resultsContainerSelector);
		let searchContainer = searchFormJq
			.parent()
			.parent()
			.parent()
			.parent()
			.parent();

		const formData = new FormData(searchFormJs);
		let itemId = formData.get("item");

		formData.delete("itemName");
		formData.delete("item");

		let searchUrl = Rest.passRoot + `/inventory/item/${itemId}/stored?` + new URLSearchParams(formData).toString();
		let headers = {
			"accept": "text/html",
			"searchFormId": searchFormJq.attr("id"),
			"actionType": "full"
			// "inputIdPrepend": itemSearchSelectModal.attr("data-bs-inputIdPrepend"),
		};
		if (showItem) {
			headers['showItem'] = "true";
		}
		if (showStorage) {
			headers['showStorage'] = "true";
		}
		if (forSelect) {
			headers['actionType'] = "select";
			headers['inputIdPrepend'] = selectDestination;
			headers['otherModalId'] = ModalUtils.getModalOfElement($("#"+selectDestination)).prop("id");
		}

		return Rest.call({
			spinnerContainer: searchContainer.get(0),
			url: searchUrl,
			method: 'GET',
			failNoResponse: null,
			failNoResponseCheckStatus: true,
			returnType: "html",
			extraHeaders: headers,
			// async: false,
			done: function (data) {
				resultsContainer.html(data);
			}
		});
	}
}