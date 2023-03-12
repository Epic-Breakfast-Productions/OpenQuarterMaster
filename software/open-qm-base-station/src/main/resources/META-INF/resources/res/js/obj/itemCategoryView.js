
const ItemCategoryView = {
	async setupItemCategoryView(container, categoryIdList){
		let promises = [];
		for (const categoryId of categoryIdList) {
			 promises.push(doRestCall({
				url: "/api/v1/inventory/item-categories/" + categoryId,
				method: "GET",
				async: false,
				done: function (data) {
					let curCat = $('<a class="badge m-1 itemCatBadge text-decoration-none" href="/categories?view='+data.id+'"></a>');

					curCat.text(data.name);

					if(data.color){
						curCat.css({"background-color":data.color, "color":data.textColor});
					} else {
						curCat.addClass("bg-secondary");
					}

					if(data.imageIds.length){
						curCat.prepend('<img class="itemCatBadgeImage" src="/api/v1/media/image/for/item_category/'+data.id+'" />');
					}
					if(data.description){
						curCat.attr("title", data.description);
					}

					container.append(curCat);
				},
				fail: function (data) {
					console.warn("Bad response from get item category attempt: " + JSON.stringify(data));
				},
				failMessagesDiv: messageDiv
			}));
		}
		return promises;
	}
};