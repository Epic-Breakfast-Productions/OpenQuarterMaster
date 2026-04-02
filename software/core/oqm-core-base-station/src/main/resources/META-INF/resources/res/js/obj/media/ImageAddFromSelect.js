import {ImageAdd} from "./ImageAdd.js";
import {Rest} from "../../Rest.js";
import {PageMessageUtils} from "../../PageMessageUtils.js";
import {ImageSearchSelect} from "./ImageSearchSelect.js";
import {PageUtility} from "../../utilClasses/PageUtility.js";

export class ImageAddFromSelect extends PageUtility{
	static formMessages = $("addImageFormMessages");
	static imageSearchSelectModalLabelCloseButton = $("#imageSearchSelectModalLabelCloseButton");
	static imageAddImageForm = $("#addImageForm");
	static imageAddTitleInput = $("#addTitleInput");
	static imageAddDescriptionInput = $("#addDescriptionInput");
	static imageUploadInput = $("#imageUploadInput");
	static imageAddKeywordInputDiv = $("#addImageForm").find(".keywordInputDiv");
	static imageAddAttInputDiv = $("#addImageForm").find(".attInputDiv");

	static resetImageAdd(){
		ImageAddFromSelect.imageAddImageForm.trigger("reset");
		ImageAddFromSelect.imageAddTitleInput.text("");
		ImageAddFromSelect.imageAddDescriptionInput.html("");
		ImageAddFromSelect.imageUploadInput.html("");
		ImageAddFromSelect.imageAddKeywordInputDiv.html("");
		ImageAddFromSelect.imageAddAttInputDiv.html("");
		ImageAdd.resetCroppie();
	}
	static {
		window.ImageAddFromSelect = this;

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

