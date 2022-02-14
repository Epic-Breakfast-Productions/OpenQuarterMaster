
var storageSearchSelectModal = $("#storageSearchSelectModal");
var storageSearchSelectForm = $("#storageSearchSelectForm");
var storageSearchSelectResults = $("#storageSearchSelectResults");


function setupStorageSearchModal(inputIdPrepend){
    storageSearchSelectModal.attr("data-bs-inputIdPrepend", inputIdPrepend);
}

function selectStorageBlock(blockName, blockId, inputIdPrepend, otherModalId){
    var nameInputId = inputIdPrepend + "Id";
    var nameInputName = inputIdPrepend + "Name";

    $("#"+nameInputId).val(blockId);
    $("#"+nameInputName).val(blockName);
}


storageSearchSelectForm.on("submit", function(event){
    event.preventDefault();
    console.log("Submitting search form.");

    var searchParams = new URLSearchParams(new FormData(event.target));
    console.log("URL search params: " + searchParams);


    var result = null;

    doRestCall({
    	spinnerContainer: storageSearchSelectModal.get(0),
    	url: "/api/storage?" + searchParams,
    	method: 'GET',
    	failNoResponse: null,
    	failNoResponseCheckStatus: true,
    	extraHeaders: {
    	    "accept": "text/html",
    	    "actionType": "select",
    	    "searchFormId": "storageSearchSelectForm",
    	    "inputIdPrepend": storageSearchSelectModal.attr("data-bs-inputIdPrepend"),
    	    "otherModalId": storageSearchSelectModal.attr("data-bs-otherModalId")
    	},
    	async: false,
    	done: function(data){
            console.log("Got data!");
            storageSearchSelectResults.html(data);
    	}
    });
});
