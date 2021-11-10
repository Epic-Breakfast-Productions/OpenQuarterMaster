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
	$('<div '+id+' class="alert alert-'+type+' alert-dismissible fade show alertMessage" role="alert">\n'+
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