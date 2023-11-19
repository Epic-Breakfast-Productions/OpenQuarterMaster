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
	}
};

FileAttachmentAddEdit.form.on("submit", FileAttachmentAddEdit.submitForm(e));