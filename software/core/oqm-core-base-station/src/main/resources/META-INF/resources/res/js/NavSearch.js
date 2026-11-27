import { PageUtility } from "./utilClasses/PageUtility.js";
import { ObjReference, ObjReferenceUtils } from "./obj/ObjReference.js";

export class NavSearch extends PageUtility {

	static navSearchInput = $('#navSearchInput');
	static navSearchForm = $('#navSearchForm');
	static navSearchTypeSelect = $('#navSearchTypeSelect');

	static updateNavSearchDestination = function(action, icon, fieldName) {
		NavSearch.navSearchForm.attr("action", action);
		NavSearch.navSearchTypeSelect.html(icon);
		NavSearch.navSearchInput.attr("name", fieldName);
	}

	static {
		window.NavSearch = NavSearch;

		this.navSearchInput.on("change", function (e){
			let newQuery = NavSearch.navSearchInput.val();

			if(ObjReferenceUtils.Parsing.isRefString(newQuery)) {
				console.log("Got new object reference: ", newQuery);

				ObjReferenceUtils.Parsing.parseReference(newQuery)
					.then(function (reference) {
						console.log("Reference from nav search: ", reference);

						switch (reference.type) {
							case "InventoryItem":
								window.location.href = Rest.webroot + "/items?view=" + reference.id;
								break;
							case "StorageBlock":
								window.location.href = Rest.webroot + "/storage?view=" + reference.id;
								break;
							//TODO:: stored item
						}
					});
			}
		});

		this.init();
	}
}
