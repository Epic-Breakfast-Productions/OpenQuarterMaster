
const ImageAddFromSelect = {

}

var addImageFormMessages = $("addImageFormMessages");
var imageSearchSelectModalLabelCloseButton = $("#imageSearchSelectModalLabelCloseButton");
var imageAddImageForm = $("#addImageForm");

var imageAddTitleInput = $("#addTitleInput");
var imageAddDescriptionInput = $("#addDescriptionInput");
var imageUploadInput = $("#imageUploadInput");
var imageAddKeywordInputDiv = imageAddImageForm.find(".keywordInputDiv");
var imageAddAttInputDiv = imageAddImageForm.find(".attInputDiv");

function resetImageAdd(){
	imageAddImageForm.trigger("reset");
	imageAddTitleInput.text("");
	imageAddDescriptionInput.html("");
	imageUploadInput.html("");
	imageAddKeywordInputDiv.html("");
	imageAddAttInputDiv.html("");
	resetCroppie();
}

imageAddImageForm.submit(function (ev) {
	ev.preventDefault();

	$uploadCrop.croppie('result', {
		type: 'blob',
		size: 'original'
	}).then(function(imageDataStr){
		console.log("Got image data.");
		let addData = new FormData();

		addData.append("fileName", imageUploadInput[0].files[0]);
		addData.append("file", imageDataStr);
		addData.append("description", imageAddDescriptionInput.val());
		addData.append("source", "user");

		console.log("Adding new image.");
		Rest.call({
			url: "/api/passthrough/media/image",
			method: "POST",
			data: addData,
			async: false,
			done: function(data) {
				console.log("New image id: " + data)
				selectImage(addData.title, data);
				imageSearchSelectModalLabelCloseButton.click();
				resetImageAdd();
			},
			fail: function(data) {
				console.warn("Bad response from image add attempt: " + JSON.stringify(data));
				PageMessages.addMessageToDiv(addImageFormMessages, "danger", "Failed to add image: " + data.responseText, "Failed", null);
			}
		});
	});
});