
const Getters = {
	InventoryItem: {
		get(itemId, doneFunc) {
			console.log("Getting inventory item \"" + itemId + "\"");
			return Rest.call({
				spinnerContainer: null,
				url: Rest.passRoot + "/inventory/item/" + itemId,
				done: function (data) {
					doneFunc(data);
				}
			});
		},
		getItemName(itemId, doneFunc) {
			console.log("Getting name for inventory item \"" + itemId + "\"");
			return Rest.call({
				spinnerContainer: null,
				url: Rest.passRoot + "/inventory/item/" + itemId,
				done: function (data) {
					console.log("Got item name: " + data.name);
					doneFunc(data.name);
				}
			});
		}
	},
	StoredItem: {
		getStoredForItem: async function(itemId, doneFunc = function(){}){
			return Rest.call({
				method: "GET",
				url: Rest.passRoot + "/inventory/item/" + itemId + "/stored",
				done: function(storedSearchResults){
					doneFunc(storedSearchResults);
				}
			});
		},
		getSingleStoredForItemInBlock: async function(itemId, blockId, doneFunc = function(){}){
			return Rest.call({
				method: "GET",
				url: Rest.passRoot + "/inventory/item/" + itemId + "/stored/inBlock/" + blockId,
				done: function(storedSearchResults){
					if(storedSearchResults.numResults === 0){
						throw new Error("No results where expected one.");
					}
					if(storedSearchResults.numResults > 1){
						throw new Error("More than one result. Expected one.");
					}
					doneFunc(storedSearchResults.results[0]);
				}
			});
		},
		getLabelForStored: async function(stored, doneFunc = function(){}){
			let storedLabel = stored["storageBlock-labelText"];

			StoredTypeUtils.runForType(
				stored,
				function(){
					storedLabel += " - " + stored.labelText;
				},
				function (){
					//TODO:: better
					storedLabel += " " + stored.labelText
				}
			);
			await doneFunc(storedLabel);
			return storedLabel;
		}
	}
}

async function getStorageBlock(blockId){
	//TODO
}


async function getStorageBlockItemData(blockId) {
	console.log("Getting item data for storage block \"" + blockId + "\"");
	return new Promise((done, fail) => {
		Rest.call({
			spinnerContainer: null,
			url: Rest.passRoot + "/inventory/item?inStorageBlock=" + blockId,
			done: done,
			fail: fail
		})
	});
}

async function getStorageBlockChildrenData(blockId) {
	console.log("Getting children of storage block \"" + blockId + "\"");
	return new Promise((done, fail) => {
		Rest.call({
			spinnerContainer: null,
			url: Rest.passRoot + "/inventory/storage-block?isChildOf="+blockId,
			done: done,
			fail: fail
		})
	});
}

function getStorageBlockLabel(blockId, doneFunc) {
	console.log("Getting label for storage block \"" + blockId + "\"");
	return Rest.call({
		spinnerContainer: null,
		url: Rest.passRoot + "/inventory/storage-block/" + blockId,
		done: function (data) {
			console.log("Got label: " + data.labelText);
			doneFunc(data.labelText);
		}
	});
}

function getImageName(imageId, doneFunc) {
	return Rest.call({
		spinnerContainer: null,
		url: Rest.passRoot + "/media/image/" + imageId,
		done: function (data) {
			doneFunc(data.title)
		}
	});
}

async function getItemCategoryChildrenData(categoryId) {
	console.log("Getting children of category \"" + categoryId + "\"");
	return new Promise((done, fail) => {
		Rest.call({
			spinnerContainer: null,
			url: Rest.passRoot + "/inventory/item-category?isChildOf=" + categoryId,
			done: done,
			fail: fail
		})
	});
}