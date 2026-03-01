import {Icons} from "./Icons.js";
import {UriUtils} from "./UriUtils.js";

export const PageMessageUtils = {
	mainMessageDiv: $("#messageDiv"),
	alertIdCount: 0,
	buildMessageDiv(type, message, heading, id, infoContent = null) {

		if (id != null) {
			id = 'id="' + id + '"'
		} else {
			id = "alert-" + PageMessageUtils.alertIdCount++;
		}
		let infoContentId = id + "-infoContent"

		let output = $('<div class="alert alert-' + type + ' alert-dismissible fade show alertMessage m-1 text-start" role="alert">\n' +
			'<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>' +
			'</div>');
		output.attr("id", id);

		let headingObj = $('<h4 class="alert-heading"></h4>');
		let infoContentObj = "";

		if (infoContent != null) {
			headingObj.append(
				$('<button type="button" class="btn btn-' + type + ' mx-2" data-bs-toggle="collapse" aria-expanded="false">' + Icons.info + '</button>')
					.attr("aria-controls", infoContentId)
					.attr("data-bs-target", "#" + infoContentId)
			);
			infoContentObj = $('<div class="collapse mt-2">\n' +
				'  <div class="card card-body card-' + type + ' bg-' + type + ' user-select-all">\n' +
				'  </div>\n' +
				'</div>');
			infoContentObj.attr("id", infoContentId)
			infoContentObj.find(".card-body").text(infoContent)
		}

		if (heading != null) {
			headingObj.append($('<span class="alert-heading-text"></span>').text(heading));
		}
		output = output.append(headingObj);
		output = output.append($('<span class="message"></span>').text(message));
		output = output.append(infoContentObj);

		return output;
	},
	addMessageToDiv(jqueryObj, type, message, heading, id, infoContent = null) {
		let messageDiv = PageMessageUtils.buildMessageDiv(type, message, heading, id, infoContent);
		messageDiv.appendTo(jqueryObj.get(0));
	},
	addMessage(type, message, heading, id, infoContent = null) {
		PageMessageUtils.addMessageToDiv(PageMessageUtils.mainMessageDiv, type, message, heading, id, infoContent);
	},
	getMessageQuery(message, type, heading) {
		let messageQuery = "message=" + message;

		if (type) {
			messageQuery += "&messageType=" + type;
		}
		if (heading) {
			messageQuery += "&messageHeading=" + heading;
		}
		return messageQuery;
	},
	reloadPageWithMessage(message, type, heading) {
		let messageQuery = PageMessageUtils.getMessageQuery(message, type, heading);
		let url = window.location.href;

		if (url.indexOf('?') > -1) {
			url += '&' + messageQuery;
		} else {
			url += '?' + messageQuery;
		}
		window.location.replace(url);
	},
	gotoPageWithMessage(page, message, type, heading) {
		let url = page + '?' + PageMessageUtils.getMessageQuery(message, type, heading);
		window.location.replace(url);
	},
	initPage() {
		console.log("Processing page message.");

		if (UriUtils.getParams.has("message")) {
			console.log("Had page message.");
			let type = (UriUtils.getParams.has("messageType") ? UriUtils.getParams.get("messageType") : "info");
			let heading = (UriUtils.getParams.has("messageHeading") ? UriUtils.getParams.get("messageHeading") : null)
			PageMessageUtils.addMessage(type, UriUtils.getParams.get("message"), heading, null);

			UriUtils.getParams.delete("message");
			UriUtils.getParams.delete("messageType");
			UriUtils.getParams.delete("messageHeading");

			let newQuery = UriUtils.getParams.toString();
			window.history.replaceState({}, document.title, window.location.href.split('?')[0] + (newQuery ? '?' + newQuery : ''));
		} else{
			console.log("No page message.");
		}
		console.log("Done processing page messages.");
		Main.processStop();
	}
};