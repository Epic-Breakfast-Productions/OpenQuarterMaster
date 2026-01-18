
const Getters = {
	InventoryItem: {
		get(itemId, doneFunc = function () {}) {
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
			//TODO:: handle paging
			return Rest.call({
				method: "GET",
				url: Rest.passRoot + "/inventory/item/" + itemId + "/stored",
				done: function(storedSearchResults){
					doneFunc(storedSearchResults);
				}
			});
		},
		getStoredForItemInBlock: async function(itemId, blockId, doneFunc = function(){}){
			//TODO:: handle paging
			return Rest.call({
				method: "GET",
				url: Rest.passRoot + "/inventory/item/" + itemId + "/block/" + blockId + "/stored",
				done: function(storedSearchResults){
					doneFunc(storedSearchResults);
				}
			});
		},
		getSingleStoredForItemInBlock: async function(itemId, blockId, doneFunc = function(){}, ifNone = null){
			return Rest.call({
				method: "GET",
				url: Rest.passRoot + "/inventory/item/" + itemId + "/block/" + blockId + "/stored",
				done: function(storedSearchResults){
					if(storedSearchResults.numResults === 0){
						if(ifNone != null){
							console.log("No results where expected one. Calling specified handler.")
							ifNone();
							return;
						} else {
							throw new Error("No results where expected one.");
						}
					}
					if(storedSearchResults.numResults > 1){
						throw new Error("More than one result. Expected one.");
					}
					doneFunc(storedSearchResults.results[0]);
				}
			});
		},
		getSingleStoredForItem: async function(itemId, doneFunc = function(){}, ifNone = null){
			return Rest.call({
				method: "GET",
				url: Rest.passRoot + "/inventory/item/" + itemId + "/stored",
				done: function(storedSearchResults){
					if(storedSearchResults.numResults === 0){
						if(ifNone != null){
							console.log("No results where expected one. Calling specified handler.")
							ifNone();
						} else {
							throw new Error("No results where expected one.");
						}
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
		},
		getStored: async function(itemId, storedId, doneFunc = function(){}){
			return Rest.call({
				method: "GET",
				url: Rest.passRoot + "/inventory/item/" + itemId + "/stored/" + storedId,
				done: function(itemStored){
					doneFunc(itemStored);
				}
			});
		},
	},
	Checkout: {
		getCheckout: async function(checkoutId, doneFunc = function(){}){
			let output = null;

			await Rest.call({
				method: "GET",
				url: Rest.passRoot + "/inventory/item-checkout/" + checkoutId,
				done: function(checkout){
					output = checkout
					doneFunc(checkout);
				}
			});
			return output;
		}
	},
	InteractingEntities: {
		getEntities: async function({type=null, doneFunc = function(){}}){
			let result = [];

			let params = new URLSearchParams();
			if(type !== null){
				params.set("type", type);
			}

			Rest.call({
				method: "GET",
				url: Rest.passRoot + "/interacting-entity?" + params.toString(),
				done: function(entitiesSearchResult){
					console.log("Got interacting entities: ", entitiesSearchResult);
					result = entitiesSearchResult.results;
					doneFunc(entitiesSearchResult.results);
				}
			});

			return result;
		}
	},
	Identifiers: {
		generator: async function(generatorId, doneFunc = function(){}) {
			let output = null;

			await Rest.call({
				method: "GET",
				url: Rest.passRoot + "/identifier/generator/" + generatorId,
				done: function(checkout){
					output = checkout
					doneFunc(checkout);
				}
			});
			return output;
		}
	}
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
	//TODO:: cache called ids
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