
export class UriUtils {
	static getParams = new URLSearchParams(window.location.search);

	static updateParams(){
		let newQuery = UriUtils.getParams.toString();
		window.history.replaceState({}, document.title, window.location.href.split('?')[0] + (newQuery? '?' + newQuery : ''));
	}
	static addOrReplaceParams(key, value){
		UriUtils.getParams.set(key, value);
		UriUtils.updateParams();
	}
	static removeParam(key){
		UriUtils.getParams.delete(key);
		UriUtils.updateParams();
	}
	static removeHash(){
		//TODO:: probably should do this smarter?
		window.history.replaceState({}, document.title, window.location.href.split('#')[0]);
	}
	static {
		window.UriUtils = this;
	}
}
