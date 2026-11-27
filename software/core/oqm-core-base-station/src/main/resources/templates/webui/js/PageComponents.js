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
}
