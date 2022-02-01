
function getStorageBlockLabel(blockId, doneFunc){
    doRestCall({
        	spinnerContainer: null,
        	url: "/api/storage/" + blockId,
        	done: function(data){
        	    doneFunc(data.label)
        	}
    });
}