// https://fperucic.github.io/treant-js/

import {Rest} from "../../Rest.js";

export const ItemCategoryTree = {
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
		let curNode = {
			text: {
				name: curBlock.catName,
				title: curBlock.catName
			},
			children: []
		};

		if (curBlock.firstImageId) {
			curNode.image = Rest.passRoot + "/media/image/" + curBlock.firstImageId + "/data";
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

		Rest.call({
//        spinnerContainer: $(containerSelector),
			url: Rest.passRoot + "/inventory/item-category/tree",
			done: function (data) {
				console.log("Successfully got tree data.");
				let rootChildrenList = chartConfig.nodeStructure.children;
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
			curCrumb.find("a").attr("href", "/itemCategories?" + newGetParams.toString());

			crumbList.append(curCrumb);
			ItemCategoryTree.addCrumbs(cur.children[0], crumbList, toKeepId);
		} else {
			let curCrumb = $('<li class="breadcrumb-item active" aria-current="page">' + cur.catName + ' (this)</li>');
			curCrumb = curCrumb.text(cur.catName + " (this)");
			crumbList.append(curCrumb);
		}
	},
	async getBreadcrumbs(crumbContainer, toKeepId) {
		Rest.call({
			url: Rest.passRoot + "/inventory/item-category/tree?onlyInclude=" + toKeepId,
			done: function (data) {
				console.log("Successfully got tree data.");

				let crumbList = $('<ol class="breadcrumb"></ol>');

				ItemCategoryTree.addCrumbs(data.rootNodes[0], crumbList, toKeepId);

				let nav = $('<nav aria-label="Storage Block Breadcrumb"></nav>');
				nav.append(crumbList);
				crumbContainer.append(nav);
			},
			fail: function () {
			}
		});
	}
};

