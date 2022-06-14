
function getStorageBlockLabel(blockId, doneFunc){
    doRestCall({
        	spinnerContainer: null,
        	url: "/api/storage/" + blockId,
        	done: function(data){
        	    doneFunc(data.label);
        	}
    });
}
function getImageName(imageId, doneFunc){
    doRestCall({
        	spinnerContainer: null,
        	url: "/api/media/image/" + imageId,
        	done: function(data){
        	    doneFunc(data.title)
        	}
    });
}