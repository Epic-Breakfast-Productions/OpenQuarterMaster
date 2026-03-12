import {ModalUtils} from "../../../ModalUtils.js";
import {FileAttachmentAddEdit} from "./FileAttachmentAddEdit.js";
import {Rest} from "../../../Rest.js";

export const FileAttachmentSearchSelect = {
	curResultContainer: null,
	selectSearch: $("#fileAttachmentSearchSelectForm"),
	modal: $("#fileAttachmentSearchSelectModal"),
	modalCloseButton: $("#fileAttachmentSearchSelectModalLabelCloseButton"),
	modalBs: new bootstrap.Modal("#fileAttachmentSearchSelectModal"),
	fileSearchResults: $("#fileAttachmentSearchSelectResults"),

	setup(resultContainerJq){
		console.log("Setting up for file attachment search select.");
		ModalUtils.setReturnModal(FileAttachmentSearchSelect.modal, resultContainerJq);
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
	},
	getFileListFromInput(inputContainerJq){
		let output = [];
		inputContainerJq.find(".selectedFile").each(function (i, selectedFileRow){
			output.push($(selectedFileRow).data("id"));
		});
		console.log("Got the following file ids: ", output);

		return output;
	},
	populateFileInputFromObject(inputContainerJq, fileIdList, spinnerContainer, failMessagesDiv){
		console.log("Populating file attachment input.");
		this.curResultContainer = inputContainerJq.find(".fileAttachmentSelectInputTableContent");

		fileIdList.forEach(function (curId){
			Rest.call({
				spinnerContainer: null,
				url: Rest.passRoot + "/media/fileAttachment/" + curId,
				failMessagesDiv: failMessagesDiv,
				done: async function (data) {
					FileAttachmentSearchSelect.selectFile(curId, data.revisions[0].origName);
				}
			});
		});
	},
	initPage: function () {
		FileAttachmentSearchSelect.selectSearch.on("submit", function (e){
			e.preventDefault();
			console.log("Submitting File Attachment Select Search");

			let searchParams = new URLSearchParams(new FormData(e.target));
			console.log("URL search params: " + searchParams);

			Rest.call({
				spinnerContainer: FileAttachmentSearchSelect.modal.get(0),
				url: Rest.passRoot + "/media/fileAttachment?" + searchParams,
				method: 'GET',
				failNoResponse: null,
				failNoResponseCheckStatus: true,
				returnType: "html",
				extraHeaders: {
					"accept": "text/html",
					"actionType": "select",
					"searchFormId": "fileAttachmentSearchSelectForm",
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

		FileAttachmentAddEdit.fileAttachmentAdded = function (newFile, name){
			console.log("Selecting newly addd file attachment: ", newFile);
			FileAttachmentSearchSelect.selectFile(newFile.id, name);
			FileAttachmentSearchSelect.modalCloseButton.click();
		}
	}
};
