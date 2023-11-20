const FileAttachmentAddEdit = {
	formMessages: $("#fileAttachmentAddEditFormMessages"),
	form: $("#fileAttachmentAddEditForm"),
	fileInput: $("#fileAttachmentAddEditFormFileInput"),

	resetForm(){
		this.fileInput.val(null);
	},
	setupForAdd(){
		this.resetForm();
	},

	submitForm(e){
		e.preventDefault();
		console.log("Submitting FileAttachmentAddEdit form.");

		// https://docs.getform.io/installations/ajax/sending-submissions-with-jquery-ajax/#uploading-files-using-jquery-ajax

	}
};

FileAttachmentAddEdit.form.on("submit", function(e){FileAttachmentAddEdit.submitForm(e)});