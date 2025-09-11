const ImageSearchSelect = {
	imageSearchSelectModal: $("#imageSearchSelectModal"),
	searchForm: $("#imageSearchSelectForm"),
	searchResults: $("#imageSearchSelectResults"),
	curImagesSelectedDiv: null,

	setupImageSearchModal(selectedImagesDiv) {
		ModalHelpers.setReturnModal(ImageSearchSelect.imageSearchSelectModal, selectedImagesDiv);
		ImageSearchSelect.curImagesSelectedDiv = selectedImagesDiv;
	},
	selectImage(imageName, imageId) {
		let newImageSelected = $(`<div class="card selectedImage g-0 p-1 m-1 text-center float-start" >
        <img src="` + Rest.passRoot + '/media/image/' + imageId + '/revision/latest/data" alt="' + imageName + '" class="card-img-top" onclick="ImageSearchSelect.removeSelectedImage(this.parentElement);" data-bs-imageId="' + imageId + `">
        <div class="input-group m-1 p-1">
            <button type="button" title="Move image up" class="btn btn-sm btn-outline-dark" onclick="ImageSearchSelect.moveImageInputUp(this.parentElement.parentElement);">&lt;</button>
            <button type="button" title="Move image down" class="btn btn-sm btn-outline-dark" onclick="ImageSearchSelect.moveImageInputDown(this.parentElement.parentElement);">&gt;</button>
        </div> \
    </div>`);

		ImageSearchSelect.curImagesSelectedDiv.append(newImageSelected);
	},
	addSelectedImages(selectedImagesDiv, imageList) {
		ImageSearchSelect.setupImageSearchModal(selectedImagesDiv);
		var titleArr = [];
		imageList.forEach(async function (imageId, i) {
			await Rest.call({
				async: false,
				spinnerContainer: null,
				url: Rest.passRoot+"/media/image/" + imageId + "/revision/latest/data",
				done: function (data) {
					titleArr[i] = data.title
				}
			});
		});

		imageList.forEach(function (imageId, i) {
			ImageSearchSelect.selectImage(titleArr[i], imageId);
		});
	},
	moveImageInputUp(imageDiv) {
		console.log("Moving image up");
		if (imageDiv.previousSibling) {
			imageDiv.parentElement.insertBefore(imageDiv, imageDiv.previousSibling);
		}
	},
	moveImageInputDown(imageDiv) {
		console.log("Moving image down");
		if (imageDiv.nextSibling) {
			if (imageDiv.nextSibling.nextSibling) {
				imageDiv.parentElement.insertBefore(imageDiv, imageDiv.nextSibling.nextSibling);
			} else {
				imageDiv.parentElement.appendChild(imageDiv);
			}
		}
	},
	removeSelectedImage(toRemove) {
		toRemove.remove();
	},
	addImagesToData(data, selectedImageDiv){
		data.imageIds = [];
		selectedImageDiv.find("img").each(function(i, curImg){
			data.imageIds.push($(curImg).attr('data-bs-imageId'));
		});
	}
};

ImageSearchSelect.searchForm.on("submit", function (event) {
	event.preventDefault();
	console.log("Submitting search form.");

	var searchParams = new URLSearchParams(new FormData(event.target));
	console.log("URL search params: " + searchParams);

	Rest.call({
		spinnerContainer: ImageSearchSelect.imageSearchSelectModal.get(0),
		url: Rest.passRoot + "/media/image?" + searchParams,
		method: 'GET',
		failNoResponse: null,
		failNoResponseCheckStatus: true,
		returnType: "html",
		extraHeaders: {
			"accept": "text/html",
			"actionType": "select",
			"searchFormId": "imageSearchSelectForm",
			"inputIdPrepend": ImageSearchSelect.imageSearchSelectModal.attr("data-bs-inputIdPrepend"),
			"otherModalId": ImageSearchSelect.imageSearchSelectModal.attr("data-bs-otherModalId")
		},
		async: false,
		done: function (data) {
			console.log("Got data!");
			ImageSearchSelect.searchResults.html(data);
		}
	});

});
