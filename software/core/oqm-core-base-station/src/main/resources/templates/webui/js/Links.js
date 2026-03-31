import { Icons } from "./Icons.js";

export class Links {
	static storage = '<a href="{rootPrefix}/storage">' + Icons.storageBlock + ' Storage</a>'

	static getStorageViewLink(id, text = "") {
		let newLink = $(Links.storage);

		newLink.prop("href", newLink.prop("href") + "?view=" + id);

		if (text) {
			newLink.html(Icons.storageBlock);

			newLink.append($(" \n<span></span>").text(text));
		}

		return newLink;
	}

	static getStorageViewButton(id, text = "") {
		let newButton = Links.getStorageViewLink(id, text);
		newButton.addClass("btn");
		newButton.addClass("btn-primary");

		return newButton;
	}

	static getStorageViewButtonAsHtml(id, text = "") {
		return Links.getStorageViewButton(id, text).prop("outerHTML");
	}


	static items = '<a href="{rootPrefix}/items">' + Icons.items + ' Items</a>';

	static getItemViewLink(id, text = "") {
		let newLink = $(Links.items);

		newLink.prop("href", newLink.prop("href") + "?view=" + id);

		if (text) {
			newLink.html(Icons.item);

			newLink.append($(" \n<span></span>").text(text));
		}

		return newLink;
	}

	static getItemViewButton(id, text = "") {
		let newButton = Links.getItemViewLink(id, text);
		newButton.addClass("btn");
		newButton.addClass("btn-primary");

		return newButton;
	}

	static getItemViewButtonAsHtml(id, text = "") {
		return Links.getItemViewButton(id, text).prop("outerHTML")
	}
}