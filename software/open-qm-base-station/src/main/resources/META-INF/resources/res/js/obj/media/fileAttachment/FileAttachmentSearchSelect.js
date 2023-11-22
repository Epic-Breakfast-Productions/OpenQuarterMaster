const FileAttachmentSearchSelect = {
	curResultContainer: null,
	selectSearch: $("#fileAttachmentSearchSelectForm"),
	modal: $("#fileAttachmentSearchSelectModal"),
	modalCloseButton: $("#fileAttachmentSearchSelectModalLabelCloseButton"),
	modalBs: new bootstrap.Modal("#fileAttachmentSearchSelectModal"),
	fileSearchResults: $("#fileAttachmentSearchSelectResults"),

	setup(resultContainerJq){
		console.log("Setting up for file attachment search select.");
		FileAttachmentAddEdit.setupForAdd();
		this.curResultContainer = resultContainerJq;
		this.selectSearch.submit();
	},

	selectFile(fileId, fileName){
		console.log("User selected file ", fileId);

		let output = $('<tr class="selectedFile"></tr>');
		output.data("id", fileId);

		output.append($('<td></td>').text(fileName));
		output.append($('<td><button type="button" class="btn btn-danger btn-sm" onclick="$(this).parent().parent().remove();" title="Remove">'+Icons.remove+'</button></td>'));

		this.curResultContainer.append(output);
	},

	resetInput(inputContainerJq){
		inputContainerJq.find(".fileAttachmentSelectInputTableContent").text("");
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
			"inputIdPrepend": FileAttachmentSearchSelect.modal.attr("data-bs-inputIdPrepend"),
			"otherModalId": FileAttachmentSearchSelect.modal.attr("data-bs-otherModalId")
		},
		async: false,
		done: function (data) {
			console.log("Got data!");
			FileAttachmentSearchSelect.fileSearchResults.html(data);
		}
	});
});

FileAttachmentAddEdit.fileAttachmentAdded = function (data){
	console.log("Selecting newly addd file attachment: ", data);
	FileAttachmentSearchSelect.selectFile(data.id, data.revisions[0].origName);
	FileAttachmentSearchSelect.modalCloseButton.click();
}