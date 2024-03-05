
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
		type: 'base64',
		size: 'original'
	}).then(function(imageDataStr){
		console.log("Got image data.");
		var addData = {
			title: imageAddTitleInput.val(),
			description: imageAddDescriptionInput.val(),
			imageData: imageDataStr
		};

		addKeywordAttData(addData, imageAddKeywordInputDiv, imageAddAttInputDiv);

		console.log("Adding new image.");
		Rest.call({
			url: "/api/v1/media/image",
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