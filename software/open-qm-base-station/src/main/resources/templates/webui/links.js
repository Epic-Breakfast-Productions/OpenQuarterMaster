
//TODO:: determine if necessary
const Links = {
	storage: '<a href="/storage">{#icons/storageBlocks}{/icons/storageBlocks} Storage</a>',

	getStorageViewLink: function (id, text=""){
		let newLink = $(Links.storage);

		newLink.prop("href", newLink.prop("href") + "?view=" + id);

		if(text){
			newLink.html(Icons.storageBlock);

			newLink.append($(" \n<span></span>").text(text));
		}

		return newLink;
	},
	getStorageViewButton: function (id, text=""){
		let newButton= Links.getStorageViewLink(id, text);
		newButton.addClass("btn");
		newButton.addClass("btn-primary");

		return newButton;
	},
	getStorageViewButtonAsHtml(id, text=""){ return Links.getStorageViewButton(id, text).prop("outerHTML")}
}