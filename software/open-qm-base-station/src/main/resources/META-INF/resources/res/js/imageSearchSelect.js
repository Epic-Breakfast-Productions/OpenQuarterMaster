
var imageSearchSelectModal = $("#imageSearchSelectModal");
var imageSearchSelectForm = $("#imageSearchSelectForm");
var imageSearchSelectResults = $("#imageSearchSelectResults");
var imagesSelected = $('#imagesSelected');


function setupImageSearchModal(inputIdPrepend){
    imageSearchSelectModal.attr("data-bs-inputIdPrepend", inputIdPrepend);
}

function selectImage(imageName, imageId){
    //TODO:: make move up/back buttons work
    var newImageSelected = $('<div class="card selectedImage g-0 p-1 m-1 text-center float-start" > \
        <img src="/api/media/image/'+imageId+'/data" alt="'+imageName+'" class="card-img-top" onclick="removeSelectedImage(this);" data-bs-imageId="'+imageId+'"> \
        <div class="input-group m-1 p-1"> \
            <button type="button" title="Move image up" class="btn btn-sm btn-outline-dark" onclick="moveImageInputUp(this.parentElement.parentElement);">&lt;</button> \
            <button type="button" title="Move image down" class="btn btn-sm btn-outline-dark" onclick="moveImageInputDown(this.parentElement.parentElement);">&gt;</button> \
        </div> \
    </div>');

    imagesSelected.append(newImageSelected);
}

function moveImageInputUp(imageDiv){
    console.log("Moving image up");
    if(imageDiv.previousSibling){
        imageDiv.parentElement.insertBefore(imageDiv, imageDiv.previousSibling);
    }
}
function moveImageInputDown(imageDiv){
    console.log("Moving image down");
    if(imageDiv.nextSibling){
        if(imageDiv.nextSibling.nextSibling){
            imageDiv.parentElement.insertBefore(imageDiv, imageDiv.nextSibling.nextSibling);
        } else {
            imageDiv.parentElement.appendChild(imageDiv);
        }
    }
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
