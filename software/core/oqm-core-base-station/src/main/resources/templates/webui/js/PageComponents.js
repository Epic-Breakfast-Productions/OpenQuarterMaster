import { Rest } from "./Rest.js";

export class PageComponents {
	static Inputs = class {
		static attribute = `{#inputs/attInput /}`;
		static keywords= `{#inputs/keywordInput /}`;
		static image= `{#search/image/imageSelectFormInput /}`;
		static file= `{#fileAttachment/fileAttachmentSelectFormInput /}`;
		static copyButton= `{#copyTextButton /}`;


		static Pricing = class {
			static priceInput = `{#inputs/pricing/pricingInput /}`;
			static storedPricing = `{#inputs/pricing/pricingInputPrice /}`;
		};


		static Identifiers = class {
			static identifierInput = `{#inputs/identifiers/associatedIdentifierInput id='' /}`;
			static identifierAdded = `{#inputs/identifiers/addedIdentifier /}`;
		};


		static AssociatedLinks = class {
			static linkInput = `{#inputs/assocLink/assocLinkInputLink /}`;
		}
	}

	static View = class {
		static itemCatBadge = `{#objView/itemCatBadge /}`;
		static attachedFileList = `{#fileAttachment/FileAttachmentObjectView /}`;
	}

	static Search = class {
		static History = class {
			static newHistorySearch = function (id, objectUrl, accordion = false) {

				let params = new URLSearchParams();
				params.set("id", id);
				params.set("objectUrl", objectUrl);
				params.set("accordion", accordion);

				return Rest.call({
					spinnerContainer: null,
					url: Rest.componentRoot + "/search/history?"+params.toString(),
					returnType: "html"
				});
			}
			static newStoredTransactionSearch = function (id, item=null, stored=null, accordion = false) {

				let params = new URLSearchParams();
				params.set("id", id);
				params.set("item", item);
				params.set("storedItem", stored);
				params.set("accordion", accordion);

				return Rest.call({
					spinnerContainer: null,
					url: Rest.componentRoot + "/search/storedTransaction?"+params.toString(),
					returnType: "html"
				});
			}
		}
	}
}
