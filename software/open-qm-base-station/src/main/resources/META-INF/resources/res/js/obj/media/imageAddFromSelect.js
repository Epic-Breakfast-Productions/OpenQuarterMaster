
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
		doRestCall({
			url: "/api/media/image",
			method: "POST",
			data: addData,
			async: false,
			done: function(data) {
				selectImage(data.title, data.id);
				imageSearchSelectModalLabelCloseButton.click();
				resetImageAdd();
			},
			fail: function(data) {
				console.warn("Bad response from image add attempt: " + JSON.stringify(data));
				addMessageToDiv(addEditFormMessages, "danger", "Failed to do add image: " + data.responseText, "Failed", null);
			}
		});
	});
});