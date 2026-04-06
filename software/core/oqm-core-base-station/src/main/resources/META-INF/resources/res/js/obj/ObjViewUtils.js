import {Rest} from "../Rest.js";
import {PageUtility} from "../utilClasses/PageUtility.js";

export class KeywordAttUtils extends PageUtility {

	static displayKeywordsIn(container, keywords) {
		keywords.forEach(function (keyword) {
			console.log("Keyword: " + keyword);
			let newspan = $('<span class="badge bg-secondary m-2 user-select-all"></span>');
			newspan.text(keyword);
			container.append(newspan);
		});
	}

	static clearHideKeywordDisplay(container) {
		container.hide();
		container.find('.keywordsViewContainer').html("");
	}

	static processKeywordDisplay(container, keywords) {
		KeywordAttUtils.clearHideKeywordDisplay(container);
		if (keywords.length > 0) {
			console.log("had keywords");
			container.show();
			KeywordAttUtils.displayKeywordsIn(container.find('.keywordsViewContainer'), keywords);
		} else {
			console.log("did not have keywords.");
		}
	}

	static getAttDisplay(key, val) {
		let output = $('<span class="badge bg-secondary m-2"><span class="attKey user-select-all"></span> <i class="fas fa-equals"></i> <code class="attVal user-select-all"></code></span>');

		output.find(".attKey").text(key);
		output.find(".attVal").text(val);

		return output;
	}

	static displayAttsIn(container, attributes) {
		Object.entries(attributes).forEach(entry => {
			const [key, val] = entry;
			console.log("Att: " + key + "/" + val);

			container.append(KeywordAttUtils.getAttDisplay(key, val));
		});
	}

	static clearHideAttDisplay(container) {
		container.hide();
		container.find('.attsViewContainer').html("");
	}

	static processAttDisplay(container, attributes) {
		KeywordAttUtils.clearHideAttDisplay(container);
		if (Object.keys(attributes).length > 0) {
			console.log("had attributes");
			container.show();
			KeywordAttUtils.displayAttsIn(container.find('.attsViewContainer'), attributes)
		} else {
			console.log("did not have attributes.");
		}
	}

	static Keywords = class {
		static async getNewDisplay(keywords = null, id = null) {
			return Rest.call({
				url: Rest.componentRoot + "/keywordDisplay",
				returnType: "html"
			}).then(function (keywordDisplay) {
				if (keywords != null) {
					KeywordAttUtils.displayKeywordsIn($(keywordDisplay), keywords)
				}
			});
		}

		//TODO:: move other related in here
	}
	static Attributes = class {
		static async getNewDisplay(atts = null, id = null) {
			return Rest.call({
				url: Rest.componentRoot + "/attDisplay",
				returnType: "html"
			}).then(function (attsDisplay) {
				if (atts != null) {
					KeywordAttUtils.displayAttsIn($(attsDisplay), atts)
				}
			});
		}

		//TODO:: move other related in here
	}

	static {
		window.KeywordAttUtils = this;
	}
}
