const FileAttachmentSearchSelect = {
	curResultContainer: null,
	selectSearch: $("#fileAttachmentSearchSelectForm"),
	fileAttachmentSearchSelectModal: $("#fileAttachmentSearchSelectModal"),
	fileSearchResults: $("#fileAttachmentSearchSelectResults"),

	/**
	 * Creates the result html to add to the results
	 */
	addResult(){
		let output = $('<tr></tr>');
		//TODO:: add id info to row

		output.append($('<td></td>').text("filename TODO"));
		output.append($('<td></td>').text("deleteButton TODO"));

		this.curResultContainer.append(output);
	},

	setup(resultContainerJq){
		console.log("Setting up for file attachment search select.");
		FileAttachmentAddEdit.setupForAdd();
		this.curResultContainer = resultContainerJq;
		this.selectSearch.submit();
	}
};

FileAttachmentSearchSelect.selectSearch.on("submit", function (e){
	e.preventDefault();
	console.log("Submitting File Attachment Select Search");

	let searchParams = new URLSearchParams(new FormData(e.target));
	console.log("URL search params: " + searchParams);

	doRestCall({
		spinnerContainer: imageSearchSelectModal.get(0),
		url: "/api/v1/media/fileAttachments?" + searchParams,
		method: 'GET',
		failNoResponse: null,
		failNoResponseCheckStatus: true,
		extraHeaders: {
			"accept": "text/html",
			"actionType": "select",
			"searchFormId": "imageSearchSelectForm",
			"inputIdPrepend": FileAttachmentSearchSelect.fileAttachmentSearchSelectModal.attr("data-bs-inputIdPrepend"),
			"otherModalId": FileAttachmentSearchSelect.fileAttachmentSearchSelectModal.attr("data-bs-otherModalId")
		},
		async: false,
		done: function (data) {
			console.log("Got data!");
			FileAttachmentSearchSelect.fileSearchResults.html(data);
		}
	});
})