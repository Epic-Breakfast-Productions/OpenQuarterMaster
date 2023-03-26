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

	getUseButton(text){
		let newButton = $('<button type="button" class="btn btn-link mb-0 p-0" title="Use this value"></button>');

		newButton.text(text + ": ");
		newButton.append(Icons.useDatapoint);

		return newButton;
	},

	createSearchResultSection(name, value, targetInput){
		let section = $('<li class="list-group-item extProdResultSection"><h6 class="card-title"></h6></li>');

		if(targetInput) {
			let useButton = ExtItemSearch.getUseButton(name);
			useButton.on("click", function (e) {
				let valElement;
				if(e.target.tagName === "I"){
					valElement = $(e.target.parentElement.parentElement.nextElementSibling);
				} else {
					valElement = $(e.target.parentElement.nextElementSibling)
				}
				targetInput.val(valElement.text());
			});
			section.children("h6").append(useButton);
		} else {
			section.children("h6").text(name + ":");
		}

		let sectionText = $('<p class="card-text"></p>');
		sectionText.text(value);
		section.append(sectionText);

		return section;
	},


	handleExtItemSearchResults(results, messagesDiv) {
		console.log("Got Results! # results: " + results.results.length + "  # errors: " + Object.keys(results.serviceErrs).length);

		if (results.results.length === 0) {
			ExtItemSearch.extSearchResults.html("<p>No Results!</p>");
		}
		results.results.forEach(function (result) {
			//TODO:: better formatting, method for filling out values
			let resultCard = $('<div class="card col-12 p-0" style="height: fit-content"></div>');
			{
				let header = $('<div class="card-header"></div>');
				header.text(result.source);
				resultCard.append(header);
			}
			let resultMainBody = $('<ul class="list-group list-group-flush"></ul>');
			resultMainBody.append(ExtItemSearch.createSearchResultSection("Name", result.unifiedName, addEditItemNameInput));
			resultMainBody.append(ExtItemSearch.createSearchResultSection("Description", result.description, addEditItemDescriptionInput));

			//TODO:: images

			if(result.attributes){
				let attsSection = $('<li class="list-group-item extProdResultSection"><h6 class="card-title">Attributes:</h6></li>');

				let attsList = $('<span></span>');
				Object.keys(result.attributes).forEach(key => {
					let val = result.attributes[key];

					let curAtt = $('<span></span>');
					curAtt.text(key + " / " + val);
					curAtt.append($("<br />"));

					attsList.append(curAtt);
				});
				attsSection.append(attsList);

				resultMainBody.append(attsSection);
			}

			resultCard.append(resultMainBody);
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