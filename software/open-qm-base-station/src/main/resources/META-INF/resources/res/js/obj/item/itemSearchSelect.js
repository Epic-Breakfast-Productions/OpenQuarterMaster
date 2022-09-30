
var itemSearchSelectModal = $("#itemSearchSelectModal");
var itemSearchSelectForm = $("#itemSearchSelectForm");
var itemSearchSelectResults = $("#itemSearchSelectResults");


function setupItemSearchModal(inputIdPrepend){
	itemSearchSelectModal.attr("data-bs-inputIdPrepend", inputIdPrepend);
}

function selectItem(blockName, blockId, inputIdPrepend, otherModalId){
    var nameInputId = inputIdPrepend + "Id";
    var nameInputName = inputIdPrepend + "Name";

    $("#"+nameInputId).val(blockId);
    $("#"+nameInputName).val(blockName);
}


itemSearchSelectForm.on("submit", function(event){
    event.preventDefault();
    console.log("Submitting search form.");

    var searchParams = new URLSearchParams(new FormData(event.target));
    console.log("URL search params: " + searchParams);

    var result = null;

    doRestCall({
    	spinnerContainer: itemSearchSelectModal.get(0),
    	url: "/api/inventory/item?" + searchParams,
    	method: 'GET',
    	failNoResponse: null,
    	failNoResponseCheckStatus: true,
    	extraHeaders: {
    	    "accept": "text/html",
    	    "actionType": "select",
    	    "searchFormId": "storageSearchSelectForm",
    	    "inputIdPrepend": itemSearchSelectModal.attr("data-bs-inputIdPrepend"),
    	    "otherModalId": itemSearchSelectModal.attr("data-bs-otherModalId")
    	},
    	async: false,
    	done: function(data){
            console.log("Got data!");
            itemSearchSelectResults.html(data);
    	}
    });
});
