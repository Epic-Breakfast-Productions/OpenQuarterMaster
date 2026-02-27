
export const UriUtils = {
	getParams: new URLSearchParams(window.location.search),
	updateParams(){
		let newQuery = UriUtils.getParams.toString();
		window.history.replaceState({}, document.title, window.location.href.split('?')[0] + (newQuery? '?' + newQuery : ''));
	},
	addOrReplaceParams(key, value){
		UriUtils.getParams.set(key, value);
		UriUtils.updateParams();
	},
	removeParam(key){
		UriUtils.getParams.delete(key);
		UriUtils.updateParams();
	},
	removeHash(){
		//TODO:: probably should do this smarter?
		window.history.replaceState({}, document.title, window.location.href.split('#')[0]);
	}
};
