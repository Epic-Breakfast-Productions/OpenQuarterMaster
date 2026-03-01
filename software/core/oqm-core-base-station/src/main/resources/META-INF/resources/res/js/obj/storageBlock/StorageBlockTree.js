import {Rest} from "../../Rest.js";

// https://fperucic.github.io/treant-js/
export const StorageBlockTree = {
	MAIN_CONFIG: {
		chart: {
			container: "",
			animateOnInit: false,
			node: {
				collapsable: true,
				HTMLclass: 'storageTreeNode'
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
				name: curBlock.blockLabel,
				title: curBlock.blockLocation
			},
			children: []
		};

		if (curBlock.firstImageId) {
			curNode.image = Rest.passRoot + "/media/image/" + curBlock.firstImageId + "/revision/latest/data";
		}

		if (curBlock.children) {
			curBlock.children.forEach(function (curChild, i) {
				StorageBlockTree.addChildrenToList(curNode.children, curChild);
			});
		}
		childrenList.push(curNode);
	},
	showTree(containerSelector) {
		chartConfig = $.extend(true, {}, StorageBlockTree.MAIN_CONFIG);

		chartConfig.chart.container = containerSelector;

		Rest.call({
//        spinnerContainer: $(containerSelector),
			url: Rest.passRoot + "/inventory/storage-block/tree",
			done: function (data) {
				console.log("Successfully got tree data.");
				let rootChildrenList = chartConfig.nodeStructure.children;
				data.rootNodes.forEach(function (curRootNode, i) {
					StorageBlockTree.addChildrenToList(rootChildrenList, curRootNode);
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
			curCrumb.find("a").text(cur.blockLabel);
			let newGetParams = new URLSearchParams(window.location.search);
			newGetParams.set("view", cur.objectId)
			curCrumb.find("a").attr("href", "/storage?" + newGetParams.toString());

			crumbList.append(curCrumb);
			StorageBlockTree.addCrumbs(cur.children[0], crumbList, toKeepId);
		} else {
			let curCrumb = $('<li class="breadcrumb-item active" aria-current="page">' + cur.blockLabel + ' (this)</li>');
			curCrumb = curCrumb.text(cur.blockLabel + " (this)");
			crumbList.append(curCrumb);
		}
	},
	getBlockBreadcrumbs: async function(crumbContainer, toKeepId) {
		Rest.call({
			url: Rest.passRoot + "/inventory/storage-block/tree?onlyInclude=" + toKeepId,
			done: function (data) {
				console.log("Successfully got tree data.");

				let crumbList = $('<ol class="breadcrumb"></ol>');

				StorageBlockTree.addCrumbs(data.rootNodes[0], crumbList, toKeepId);

				let nav = $('<nav aria-label="Storage Block Breadcrumb"></nav>');
				nav.append(crumbList);
				crumbContainer.append(nav);
			},
			fail: function () {
			}
		});
	}
}
