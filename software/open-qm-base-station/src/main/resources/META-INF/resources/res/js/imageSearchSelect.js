
var imageSearchSelectModal = $("#imageSearchSelectModal");
var imageSearchSelectForm = $("#imageSearchSelectForm");
var imageSearchSelectResults = $("#imageSearchSelectResults");
var imagesSelected = $('#imagesSelected');


function setupImageSearchModal(inputIdPrepend){
    imageSearchSelectModal.attr("data-bs-inputIdPrepend", inputIdPrepend);
}

function selectImage(imageName, imageId){
    var newImageSelected = $('<div class="input-group mb-1" onclick="removeSelectedImage(this);"> \
      <input type="hidden" name="imageId" value="'+imageId+'" required> \
      <img src="/api/media/image/'+imageId+'/data" alt="'+imageName+'" class="selectedImage"> \
    </div>');

    imagesSelected.append(newImageSelected);
}

function removeSelectedImage(toRemove){
    toRemove.remove();
}

imageSearchSelectForm.on("submit", function(event){
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
    	done: function(data){
            console.log("Got data!");
            imageSearchSelectResults.html(data);
    	}
    });

});
