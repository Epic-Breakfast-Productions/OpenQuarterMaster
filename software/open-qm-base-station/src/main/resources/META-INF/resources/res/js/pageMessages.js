var messageDiv = $("#messageDiv")
function addMessageToDiv(jqueryObj, type, message, heading, id){
	if(heading != null){
		heading = '<h4 class="alert-heading">'+heading+'</h4>';
	}else{
		heading = "";
	}
	if(id != null){
		id = 'id="'+id+'"'
	}else{
		id = "";
	}
	$('<div '+id+' class="alert alert-'+type+' alert-dismissible fade show alertMessage m-1" role="alert">\n'+
		'<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>' +
		heading + "\n" +
		'<span class="message">\n' +
		message + "\n" +
		'</span>\n' +
		'</div>').appendTo(jqueryObj.get(0))
}
function addMessage(type, message, heading, id){
	addMessageToDiv(messageDiv, type, message, heading, id);
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


var getParams = new URLSearchParams(window.location.search);

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