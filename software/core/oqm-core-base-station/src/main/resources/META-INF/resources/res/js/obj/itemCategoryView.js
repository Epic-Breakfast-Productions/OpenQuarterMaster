
const ItemCategoryView = {
	async setupItemCategoryView(container, categoryIdList){
		let promises = [];
		for (const categoryId of categoryIdList) {
			 promises.push(Rest.call({
				url: Rest.passRoot + "/inventory/item-category/" + categoryId,
				method: "GET",
				async: false,
				done: function (data) {
					let curCat = $(PageComponents.View.itemCatBadge);

					curCat.prop('href', Rest.webroot+'/itemCategories?view='+data.id);
					curCat.find(".itemCatBadgeText").text(data.name);

					if(data.color){
						curCat.css({"background-color":data.color, "color":data.textColor});
					} else {
						curCat.addClass("bg-secondary");
					}

					if(data.imageIds.length){
						let badgeImage = curCat.find(".itemCatBadgeImage");
						badgeImage.prop('src', Rest.passRoot+'/media/image/for/item_category/'+data.id);
						badgeImage.removeClass("d-none");
					}
					if(data.description){
						curCat.attr("title", data.description);
					}

					container.append(curCat);
				},
				fail: function (data) {
					console.warn("Bad response from get item category attempt: " + JSON.stringify(data));
				},
				failMessagesDiv: PageMessageUtils.mainMessageDiv
			}));
		}
		return promises;
	}
};