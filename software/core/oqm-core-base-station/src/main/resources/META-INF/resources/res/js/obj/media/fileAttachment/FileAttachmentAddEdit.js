//TODO:: handle edit
const FileAttachmentAddEdit = {
	formMessages: $("#fileAttachmentAddEditFormMessages"),
	form: $("#fileAttachmentAddEditForm"),
	fileInput: $("#fileAttachmentAddEditFormFileInput"),
	descriptionInput: $("#fileAttachmentAddEditFormDescriptionInput"),

	resetForm(){
		this.fileInput.val(null);
		this.descriptionInput.val("");
	},
	setupForAdd(){
		this.resetForm();
	},

	submitForm(e){
		e.preventDefault();
		console.log("Submitting FileAttachmentAddEdit form.");

		// https://docs.getform.io/installations/ajax/sending-submissions-with-jquery-ajax/#uploading-files-using-jquery-ajax
		let formData = new FormData();
		let file = FileAttachmentAddEdit.fileInput[0].files[0];

		formData.append("fileName", file.name);
		formData.append("file", file);
		formData.append("source", "user");
		formData.append("description", FileAttachmentAddEdit.descriptionInput.val());

		Rest.call({
			url: Rest.passRoot + '/media/fileAttachment',
			method: "post",
			data: formData,
			done: function (data){
				console.log("Successfully added file attachment.");
				FileAttachmentAddEdit.fileAttachmentAdded(data, FileAttachmentAddEdit.fileInput[0].files[0].name);
			},
			failMessagesDiv: FileAttachmentAddEdit.formMessages
		});
	},
	fileAttachmentAdded(data) {
		PageMessageUtils.reloadPageWithMessage("Added file successfully!", "success", "Success!");
	},
	removeFile(fileId){
		console.log("Attempting to remove file: ", fileId);
		if(!confirm("Are you sure? This cannot be undone.")){
			console.log("User chose not to delete after all.");
			return;
		}
		Rest.call({
			url: Rest.passRoot + '/media/fileAttachment/' + fileId,
			method: "delete",
			done: function (data){
				console.log("Successfully removed file attachment.");
				PageMessageUtils.reloadPageWithMessage("Removed file successfully!", "success", "Success!");
			},
			failMessagesDiv: PageMessageUtils.mainMessageDiv
		});
	}
};

FileAttachmentAddEdit.form.on("submit", function(e){FileAttachmentAddEdit.submitForm(e)});