import {ImageAdd} from "./ImageAdd.js";
import {Rest} from "../../Rest.js";
import {PageMessageUtils} from "../../PageMessageUtils.js";
import {ImageSearchSelect} from "./ImageSearchSelect.js";

export const ImageAddFromSelect = {
	formMessages: $("addImageFormMessages"),
	imageSearchSelectModalLabelCloseButton: $("#imageSearchSelectModalLabelCloseButton"),
	imageAddImageForm: $("#addImageForm"),
	imageAddTitleInput: $("#addTitleInput"),
	imageAddDescriptionInput: $("#addDescriptionInput"),
	imageUploadInput: $("#imageUploadInput"),
	imageAddKeywordInputDiv: $("#addImageForm").find(".keywordInputDiv"),
	imageAddAttInputDiv: $("#addImageForm").find(".attInputDiv"),
	resetImageAdd(){
		ImageAddFromSelect.imageAddImageForm.trigger("reset");
		ImageAddFromSelect.imageAddTitleInput.text("");
		ImageAddFromSelect.imageAddDescriptionInput.html("");
		ImageAddFromSelect.imageUploadInput.html("");
		ImageAddFromSelect.imageAddKeywordInputDiv.html("");
		ImageAddFromSelect.imageAddAttInputDiv.html("");
		ImageAdd.resetCroppie();
	},
	initPage: function () {
		ImageAddFromSelect.imageAddImageForm.submit(function (ev) {
			ev.preventDefault();

			ImageAdd.uploadCrop.croppie('result', {
				type: 'blob',
				size: 'original'
			}).then(function(imageDataStr){
				console.log("Got image data.");
				let addData = new FormData();

				addData.append("fileName", ImageAddFromSelect.imageUploadInput[0].files[0].name);
				addData.append("description", ImageAddFromSelect.imageAddDescriptionInput.val());
				addData.append("source", "user");
				addData.append("file", imageDataStr);

				console.log("Adding new image.");
				Rest.call({
					url: Rest.passRoot + "/media/image",
					method: "POST",
					data: addData,
					dataType: false,
					async: false,
					done: function(data) {
						console.log("New image id: " + data.id)
						ImageSearchSelect.selectImage(addData.title, data.id);
						ImageAddFromSelect.imageSearchSelectModalLabelCloseButton.click();
						ImageAddFromSelect.resetImageAdd();
					},
					fail: function(data) {
						console.warn("Bad response from image add attempt: ", data);
						PageMessageUtils.addMessageToDiv(ImageAddFromSelect.formMessages, "danger", "Failed to add image: " + data.responseText, "Failed", null);
					}
				});
			});
		});
	}
}

