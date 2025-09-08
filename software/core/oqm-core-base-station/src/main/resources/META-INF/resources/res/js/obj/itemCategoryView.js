
const ItemCategoryView = {
	async setupItemCategoryView(container, categoryIdList){
		let promises = [];
		for (const categoryId of categoryIdList) {
			 promises.push(Rest.call({
				url: Rest.passRoot + "/inventory/item-category/" + categoryId,
				method: "GET",
				async: false,
				done: function (data) {
					let curCat = $('<a class="badge m-1 itemCatBadge text-decoration-none" href="'+Rest.webroot+'/itemCategories?view='+data.id+'"></a>');

					curCat.text(data.name);

					if(data.color){
						curCat.css({"background-color":data.color, "color":data.textColor});
					} else {
						curCat.addClass("bg-secondary");
					}

					if(data.imageIds.length){
						curCat.prepend('<img class="itemCatBadgeImage" src="'+Rest.passRoot+'/media/image/for/item_category/'+data.id+'" />');
					}
					if(data.description){
						curCat.attr("title", data.description);
					}

					container.append(curCat);
				},
				fail: function (data) {
					console.warn("Bad response from get item category attempt: " + JSON.stringify(data));
				},
				failMessagesDiv: PageMessages.mainMessageDiv
			}));
		}
		return promises;
	}
};