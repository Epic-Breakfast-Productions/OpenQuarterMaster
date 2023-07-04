
const Getters = {
	InventoryItem: {
		getItemName(itemId, doneFunc) {
			console.log("Getting name for inventory item \"" + itemId + "\"");
			return doRestCall({
				spinnerContainer: null,
				url: "/api/v1/inventory/item/" + itemId,
				done: function (data) {
					console.log("Got label: " + data.name);
					doneFunc(data.name);
				}
			});
		}
	}
}

async function getStorageBlock(blockId){
	//TODO
}


async function getStorageBlockItemData(blockId) {
	console.log("Getting item data for storage block \"" + blockId + "\"");
	return new Promise((done, fail) => {
		doRestCall({
			spinnerContainer: null,
			url: "/api/v1/inventory/item/inStorageBlock/" + blockId,
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
			url: "/api/v1/inventory/storage-block/" + blockId + "/children",
			done: done,
			fail: fail
		})
	});
}

function getStorageBlockLabel(blockId, doneFunc) {
	console.log("Getting label for storage block \"" + blockId + "\"");
	return doRestCall({
		spinnerContainer: null,
		url: "/api/v1/inventory/storage-block/" + blockId,
		done: function (data) {
			console.log("Got label: " + data.label);
			doneFunc(data.label);
		}
	});
}

function getImageName(imageId, doneFunc) {
	return doRestCall({
		spinnerContainer: null,
		url: "/api/v1/media/image/" + imageId,
		done: function (data) {
			doneFunc(data.title)
		}
	});
}

async function getItemCategoryChildrenData(categoryId) {
	console.log("Getting children of category \"" + categoryId + "\"");
	return new Promise((done, fail) => {
		doRestCall({
			spinnerContainer: null,
			url: "/api/v1/inventory/item-categories/" + categoryId + "/children",
			done: done,
			fail: fail
		})
	});
}