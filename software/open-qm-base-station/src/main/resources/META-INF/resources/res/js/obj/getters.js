function getStorageBlockLabel(blockId, doneFunc) {
	console.log("Getting label for storage block \"" + blockId + "\"");
	doRestCall({
		spinnerContainer: null,
		url: "/api/inventory/storage-block/" + blockId,
		done: function (data) {
			console.log("Got label: " + data.label);
			doneFunc(data.label);
		}
	});
}

function getImageName(imageId, doneFunc) {
	doRestCall({
		spinnerContainer: null,
		url: "/api/media/image/" + imageId,
		done: function (data) {
			doneFunc(data.title)
		}
	});
}