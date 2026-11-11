import { PageUtility } from "../utilClasses/PageUtility.js";
import { Rest } from "../Rest.js";

export class ObjReference {
	type;
	id;
	id2;

	constructor(type, id, id2 = null) {
		this.type = type;
		this.id = id;
		this.id2 = id2;
	}
}

export class ObjReferenceUtils extends PageUtility {

	static Parsing = class {
		static refStringCheckPattern = /^(oqm:|OQM:)?[a-zA-Z]+:[a-zA-Z0-9]+(:[a-zA-Z0-9]+)?$/;
		static isRefString(string){
			return ObjReferenceUtils.Parsing.refStringCheckPattern.test(string);
		}
		static parseReference(refString){
			return Rest.call({
				url: Rest.apiRoot + "/media/objRef/parse/" + refString,
				done: function (data){
					console.log("Got parsed obj ref: ", data);
					return new ObjReference(data.type, data.id, data.id2);
				}
			});
		}
	}

	static Generation = class {

		static #getUrlParamsWithRef(ref){
			let params = new URLSearchParams();

			params.set("objType", ref.type);
			params.set("id", ref.id);

			if(ref.id2){
				params.set("id2", ref.id2);
			}

			return params;
		}

		static getRefStringUrl = function(objRef){
			console.log("Building url for object reference code image: ", objRef, type);

			let params = ObjReferenceUtils.Generation.#getUrlParamsWithRef(objRef);

			return Rest.apiRoot + "/media/objRef?" + params.toString();
		}

		/**
		 *
		 * @param objRef the object reference to use
		 * @param type the type of code to get
		 * @param label If to include the label in the code image
		 */
		static getCodeUrl({
			objRef,
			includeOqm = true,
			type= "barcode",
			label= true
		}){
			console.log("Building url for object reference code image: ", objRef, type);

			let params = ObjReferenceUtils.Generation.#getUrlParamsWithRef(objRef);

			params.set("includeOqm", includeOqm);
			params.set("imageType", type);
			params.set("label", label);

			return Rest.apiRoot + "/media/objRef/image?" + params.toString();
		}
	}


	static {
		window.ObjReference = ObjReference;
		window.ObjReferenceUtils = this;
		this.init();
	}
}
