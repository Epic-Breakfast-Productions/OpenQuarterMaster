async function getStorageBlock(blockId){
	//TODO
}


async function getStorageBlockItemData(blockId) {
	console.log("Getting item data for storage block \"" + blockId + "\"");
	return new Promise((done, fail) => {
		doRestCall({
			spinnerContainer: null,
			url: "/api/inventory/item/inStorageBlock/" + blockId,
			done: done,
			fail: fail
		})
	});
}

async function getStorageBlockChildrenData(blockId) {
	console.log("Getting children of storage block \"" + blockId + "\"");
	return new Promise((done, fail) => {
		doRestCall({
			spinnerContainer: null,
			url: "/api/inventory/storage-block/" + blockId + "/children",
			done: done,
			fail: fail
		})
	});
}

function getStorageBlockLabel(blockId, doneFunc) {
	console.log("Getting label for storage block \"" + blockId + "\"");
	return doRestCall({
		spinnerContainer: null,
		url: "/api/inventory/storage-block/" + blockId,
		done: function (data) {
			console.log("Got label: " + data.label);
			doneFunc(data.label);
		}
	});
}

function getImageName(imageId, doneFunc) {
	return doRestCall({
		spinnerContainer: null,
		url: "/api/media/image/" + imageId,
		done: function (data) {
			doneFunc(data.title)
		}
	});
}