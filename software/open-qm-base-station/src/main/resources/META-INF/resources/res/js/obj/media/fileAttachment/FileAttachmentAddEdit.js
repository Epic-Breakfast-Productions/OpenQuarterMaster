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
		formData.append("description", FileAttachmentAddEdit.descriptionInput.val())

		doRestCall({
			url: '/api/v1/media/fileAttachments',
			method: "post",
			data: formData,
			done: function (data){
				console.log("Successfully added file attachment.");
				FileAttachmentAddEdit.fileAttachmentAdded(data);
			},
			failMessagesDiv: FileAttachmentAddEdit.formMessages
		});
	},
	fileAttachmentAdded(data) {
		PageMessages.reloadPageWithMessage("Added item(s) successfully!", "success", "Success!");
	}
};

FileAttachmentAddEdit.form.on("submit", function(e){FileAttachmentAddEdit.submitForm(e)});