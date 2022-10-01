var getParams = new URLSearchParams(window.location.search);

function updateParams(){
	var newQuery = getParams.toString();
	window.history.replaceState({}, document.title, window.location.href.split('?')[0] + (newQuery? '?' + newQuery : ''));
}

function addOrReplaceParams(key, value){
	getParams.set(key, value);
	updateParams();
}
function removeParam(key){
	getParams.delete(key);
	updateParams();
}