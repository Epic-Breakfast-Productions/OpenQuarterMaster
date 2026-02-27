import { Icons } from "./Icons.js";

//TODO:: determine if necessary
export const Links = {
	storage: '<a href="{rootPrefix}/storage">'+Icons.storageBlock+' Storage</a>',

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
	getStorageViewButtonAsHtml(id, text=""){ return Links.getStorageViewButton(id, text).prop("outerHTML")},


	items: '<a href="{rootPrefix}/items">'+Icons.items+' Items</a>',
	getItemViewLink: function (id, text=""){
		let newLink = $(Links.items);

		newLink.prop("href", newLink.prop("href") + "?view=" + id);

		if(text){
			newLink.html(Icons.item);

			newLink.append($(" \n<span></span>").text(text));
		}

		return newLink;
	},
	getItemViewButton: function (id, text=""){
		let newButton= Links.getItemViewLink(id, text);
		newButton.addClass("btn");
		newButton.addClass("btn-primary");

		return newButton;
	},
	getItemViewButtonAsHtml(id, text=""){ return Links.getItemViewButton(id, text).prop("outerHTML")},
}