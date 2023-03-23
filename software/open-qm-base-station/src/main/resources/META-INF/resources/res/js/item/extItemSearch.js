const ExtItemSearch = {
	extSearchResults: $("#extSearchResults"),

	prodBarcodeSearchForm: $("#prodBarcodeSearchForm"),
	prodBarcodeSearchFormMessages: $("#prodBarcodeSearchFormMessages"),
	legoPartNumSearchForm: $("#legoPartNumSearchForm"),
	legoPartNumSearchFormMessages: $("#legoPartNumSearchFormMessages"),
	websiteScanSearchForm: $("#websiteScanSearchForm"),
	websiteScanSearchFormMessages: $("#websiteScanSearchFormMessages"),

	prodBarcodeSearchBarcodeInput: $("#prodBarcodeSearchBarcodeInput"),
	legoPartNumSearchInput: $("#legoPartNumSearchInput"),
	websiteScanSearchInput: $("#websiteScanSearchInput"),

	addEditProductSearchPane: $("#addEditProductSearchPane"),

	handleExtItemSearchResults(results, messagesDiv) {
		console.log("Got Results! # results: " + results.results.length + "  # errors: " + Object.keys(results.serviceErrs).length)

		if (results.results.length === 0) {
			ExtItemSearch.extSearchResults.html("<p>No Results!</p>");
		}
		results.results.forEach(function (result) {
			//TODO:: better formatting, handle many results well
			let resultCard = $('<div class="card col-6 p-0"></div>');

			resultCard.append($('<div class="card-header">' + result.source + '</div>'));
			resultCard.append($('<div class="card-body">Name: ' + result.unifiedName + '</div>'));

			if (result.brand) {
				'<div class="card-body">Brand: ' + result.brand + '</div>'
			}
			if (result.description) {
				'<div class="card-body">Description: ' + result.description + '</div>'
			}

			ExtItemSearch.extSearchResults.append(resultCard);
		});

		for (const [service, error] of Object.entries(results.serviceErrs)) {
			addMessageToDiv(messagesDiv, "danger", error, "Failed calling " + service);
		}
	},


	toggleAddEditProductSearchPane() {
		ExtItemSearch.addEditProductSearchPane.toggle();
	},

	hideAddEditProductSearchPane() {
		ExtItemSearch.addEditProductSearchPane.hide();
	}
	,

	showAddEditProductSearchPane() {
		ExtItemSearch.addEditProductSearchPane.show();
	}
}

ExtItemSearch.websiteScanSearchForm.submit(function (event) {
	event.preventDefault();
	let webpage = ExtItemSearch.websiteScanSearchInput.val();
	console.log("Scanning a web page: " + webpage);
	ExtItemSearch.extSearchResults.html("");

	doRestCall({
		url: "/api/v1/externalItemLookup/webpage/scrape/" + encodeURIComponent(webpage),
		done: function(data){ExtItemSearch.handleExtItemSearchResults(data, ExtItemSearch.websiteScanSearchFormMessages)},
		failMessagesDiv: ExtItemSearch.websiteScanSearchFormMessages
	});
});

ExtItemSearch.prodBarcodeSearchForm.submit(function (event) {
	event.preventDefault();
	let barcodeText = ExtItemSearch.prodBarcodeSearchBarcodeInput.val();
	console.log("Searching for a barcode: " + barcodeText);
	ExtItemSearch.extSearchResults.html("");

	doRestCall({
		url: "/api/v1/externalItemLookup/product/barcode/" + barcodeText,
		done: function(data){ExtItemSearch.handleExtItemSearchResults(data, ExtItemSearch.prodBarcodeSearchFormMessages)},
		failMessagesDiv: ExtItemSearch.prodBarcodeSearchFormMessages
	});
});

ExtItemSearch.legoPartNumSearchForm.submit(function (event) {
	event.preventDefault();
	let partNumber = ExtItemSearch.legoPartNumSearchInput.val();
	console.log("Searching for a lego part: " + partNumber);
	ExtItemSearch.extSearchResults.html("");

	doRestCall({
		url: "/api/v1/externalItemLookup/lego/part/" + partNumber,
		done: function(data){ExtItemSearch.handleExtItemSearchResults(data, ExtItemSearch.legoPartNumSearchFormMessages)},
		failMessagesDiv: ExtItemSearch.legoPartNumSearchFormMessages
	});
});