var imageSearchSelectModal = $("#imageSearchSelectModal");
var imageSearchSelectForm = $("#imageSearchSelectForm");
var imageSearchSelectResults = $("#imageSearchSelectResults");
var curImagesSelectedDiv = null;

function setupImageSearchModal(selectedImagesDiv) {
	curImagesSelectedDiv = selectedImagesDiv;
}

function selectImage(imageName, imageId) {
	var newImageSelected = $('<div class="card selectedImage g-0 p-1 m-1 text-center float-start" > \
        <img src="/api/media/image/' + imageId + '/data" alt="' + imageName + '" class="card-img-top" onclick="removeSelectedImage(this.parentElement);" data-bs-imageId="' + imageId + '"> \
        <div class="input-group m-1 p-1"> \
            <button type="button" title="Move image up" class="btn btn-sm btn-outline-dark" onclick="moveImageInputUp(this.parentElement.parentElement);">&lt;</button> \
            <button type="button" title="Move image down" class="btn btn-sm btn-outline-dark" onclick="moveImageInputDown(this.parentElement.parentElement);">&gt;</button> \
        </div> \
    </div>');

	curImagesSelectedDiv.append(newImageSelected);
}

function addSelectedImages(selectedImagesDiv, imageList) {
	setupImageSearchModal(selectedImagesDiv);
	var titleArr = [];
	imageList.forEach(function (imageId, i) {
		doRestCall({
			async: false,
			spinnerContainer: null,
			url: "/api/media/image/" + imageId,
			done: function (data) {
				titleArr[i] = data.title
			}
		});
	});

	imageList.forEach(function (imageId, i) {
		selectImage(titleArr[i], imageId);
	});
}

function moveImageInputUp(imageDiv) {
	console.log("Moving image up");
	if (imageDiv.previousSibling) {
		imageDiv.parentElement.insertBefore(imageDiv, imageDiv.previousSibling);
	}
}

function moveImageInputDown(imageDiv) {
	console.log("Moving image down");
	if (imageDiv.nextSibling) {
		if (imageDiv.nextSibling.nextSibling) {
			imageDiv.parentElement.insertBefore(imageDiv, imageDiv.nextSibling.nextSibling);
		} else {
			imageDiv.parentElement.appendChild(imageDiv);
		}
	}
}

function removeSelectedImage(toRemove) {
	toRemove.remove();
}

imageSearchSelectForm.on("submit", function (event) {
	event.preventDefault();
	console.log("Submitting search form.");

	var searchParams = new URLSearchParams(new FormData(event.target));
	console.log("URL search params: " + searchParams);


	var result = null;

	doRestCall({
		spinnerContainer: imageSearchSelectModal.get(0),
		url: "/api/media/image?" + searchParams,
		method: 'GET',
		failNoResponse: null,
		failNoResponseCheckStatus: true,
		extraHeaders: {
			"accept": "text/html",
			"actionType": "select",
			"searchFormId": "imageSearchSelectForm",
			"inputIdPrepend": imageSearchSelectModal.attr("data-bs-inputIdPrepend"),
			"otherModalId": imageSearchSelectModal.attr("data-bs-otherModalId")
		},
		async: false,
		done: function (data) {
			console.log("Got data!");
			imageSearchSelectResults.html(data);
		}
	});

});
