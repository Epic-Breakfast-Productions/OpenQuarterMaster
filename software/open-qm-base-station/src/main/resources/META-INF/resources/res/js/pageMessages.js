var messageDiv = $("#messageDiv")
var alertIdCount = 0;

function buildMessageDiv(type, message, heading, id, infoContent){

	if(id != null){
		id = 'id="'+id+'"'
	}else{
		id = "alert-" + alertIdCount++;
	}
	let infoContentId = id + "-infoContent"

	let output = $('<div class="alert alert-'+type+' alert-dismissible fade show alertMessage m-1" role="alert">\n'+
		'<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>' +
		'</div>');
	output.attr("id", id);

	let headingObj = $('<h4 class="alert-heading"></h4>');
	let infoContentObj = "";

	if(infoContent != null){
		headingObj.append(
			$('<button type="button" class="btn btn-'+type+' mx-2" data-bs-toggle="collapse" aria-expanded="false"><i class="fa-solid fa-info"></i></button>')
				.attr("aria-controls", infoContentId)
				.attr("data-bs-target", "#" + infoContentId)
		);
		infoContentObj = $('<div class="collapse mt-2">\n' +
			'  <div class="card card-body card-'+type+' bg-'+type+'">\n' +
			'  </div>\n' +
			'</div>');
		infoContentObj.attr("id", infoContentId)
		infoContentObj.find(".card-body").text(infoContent)
	}

	if(heading != null){
		headingObj.append($('<span class=""></span>').text(heading));
	}
	output = output.append(headingObj);
	output = output.append($('<span class="message"></span>').text(message));
	output = output.append(infoContentObj);

	return output;
}


function addMessageToDiv(jqueryObj, type, message, heading, id, infoContent){
	buildMessageDiv(type, message, heading, id, infoContent).appendTo(jqueryObj.get(0))
}

function addMessage(type, message, heading, id, infoContent){
	addMessageToDiv(messageDiv, type, message, heading, id, infoContent);
}

function reloadPageWithMessage(message, type, heading){
    var messageQuery = "message=" + message;

    if(type){
        messageQuery += "&messageType=" + type;
    }
    if(heading){
        messageQuery += "&messageHeading=" + heading;
    }

    var url = window.location.href;
    if (url.indexOf('?') > -1){
        url += '&' + messageQuery;
    }else{
        url += '?' + messageQuery;
    }
    window.location.replace(url);
}




if(getParams.has("message")){
    var type = (getParams.has("messageType") ? getParams.get("messageType") : "info");
    var heading = (getParams.has("messageHeading") ? getParams.get("messageHeading") : null)
    addMessage(type, getParams.get("message"), heading, null);

    getParams.delete("message");
    getParams.delete("messageType");
    getParams.delete("messageHeading");

    var newQuery = getParams.toString();
    window.history.replaceState({}, document.title, window.location.href.split('?')[0] + (newQuery? '?' + newQuery : ''));
}