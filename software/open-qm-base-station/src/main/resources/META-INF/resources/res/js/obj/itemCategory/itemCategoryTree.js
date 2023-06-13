// https://fperucic.github.io/treant-js/

const ItemCategoryTree = {
	MAIN_CONFIG: {
		chart: {
			container: "",
			animateOnInit: false,
			node: {
				collapsable: true,
				HTMLclass: 'itemCategoryNode'
			},
			hideRootNode: true,
			animation: {
				nodeAnimation: "linear",
				nodeSpeed: 100,
				connectorsAnimation: "linear",
				connectorsSpeed: 100
			}
		},
		nodeStructure: {
			children: []
		}
	},

	addChildrenToList(childrenList, curBlock) {
		var curNode = {
			text: {
				name: curBlock.catName,
				title: curBlock.catName
			},
			children: []
		};

		if (curBlock.firstImageId) {
			curNode.image = "/api/v1/media/image/" + curBlock.firstImageId + "/data";
		}

		if (curBlock.children) {
			curBlock.children.forEach(function (curChild, i) {
				ItemCategoryTree.addChildrenToList(curNode.children, curChild);
			});
		}
		childrenList.push(curNode);
	},
	showTree(containerSelector) {
		let chartConfig = $.extend(true, {}, ItemCategoryTree.MAIN_CONFIG);

		chartConfig.chart.container = containerSelector;

		doRestCall({
//        spinnerContainer: $(containerSelector),
			url: "/api/v1/inventory/item-categories/tree",
			done: function (data) {
				console.log("Successfully got tree data.");
				var rootChildrenList = chartConfig.nodeStructure.children;
				data.rootNodes.forEach(function (curRootNode, i) {
					ItemCategoryTree.addChildrenToList(rootChildrenList, curRootNode);
				});

				console.debug("Treant json: " + JSON.stringify(chartConfig));
				new Treant(chartConfig);
			},
			fail: function () {
			}
		});
	},
	addCrumbs(cur, crumbList, toKeepId) {
		if (cur.objectId != toKeepId) {
			let curCrumb = $('<li class="breadcrumb-item"><a href="#"></a></li>');
			curCrumb.find("a").text(cur.catName);
			let newGetParams = new URLSearchParams(window.location.search);
			newGetParams.set("view", cur.objectId)
			curCrumb.find("a").attr("href", "/categories?" + newGetParams.toString());

			crumbList.append(curCrumb);
			ItemCategoryTree.addCrumbs(cur.children[0], crumbList, toKeepId);
		} else {
			let curCrumb = $('<li class="breadcrumb-item active" aria-current="page">' + cur.catName + ' (this)</li>');
			curCrumb = curCrumb.text(cur.catName + " (this)");
			crumbList.append(curCrumb);
		}
	},
	async getBreadcrumbs(crumbContainer, toKeepId) {
		doRestCall({
			url: "/api/v1/inventory/item-categories/tree?onlyInclude=" + toKeepId,
			done: function (data) {
				console.log("Successfully got tree data.");

				var crumbList = $('<ol class="breadcrumb"></ol>');

				ItemCategoryTree.addCrumbs(data.rootNodes[0], crumbList, toKeepId);

				var nav = $('<nav aria-label="Storage Block Breadcrumb"></nav>');
				nav.append(crumbList);
				crumbContainer.append(nav);
			},
			fail: function () {
			}
		});
	}
};

