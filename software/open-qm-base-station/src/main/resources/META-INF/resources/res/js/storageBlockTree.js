var MAIN_CONFIG = {
    chart: {
        container: "",
        animateOnInit: false,
        node: {
            collapsable: true,
            HTMLclass: 'storageTreeNode'
        },
        hideRootNode:true,
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
};

function addChildrenToList(childrenList, curBlock){
    var curNode = {
            text: {
                name: curBlock.blockLabel,
                title: curBlock.blockLocation
            },
            children: []
        };

    //TODO:: image
//    if(curBlock)


    if(curBlock.children){
        curBlock.children.forEach(function(curChild, i){
            addChildrenToList(curNode.children, curChild);
        });
    }
    childrenList.push(curNode);
}

function showTree(containerSelector){
    chartConfig = $.extend(true,{}, MAIN_CONFIG);

    chartConfig.chart.container = containerSelector;

//    new Treant(chartConfig);
//    return;

    doRestCall({
//        spinnerContainer: $(containerSelector),
        url: "/api/storage/tree",
        done: function(data){
            console.log("Successfully got tree data.");
            var rootChildrenList = chartConfig.nodeStructure.children;
            data.rootNodes.forEach(function(curRootNode, i){
                addChildrenToList(rootChildrenList, curRootNode);
            });

            console.debug("Treant json: " + JSON.stringify(chartConfig));
            new Treant(chartConfig);
        },
        fail: function(){}
    });

    // {
//                       text: {name: "Lana"},
//                       collapsed: true,
//                       children: [
//                           {
//                               text: {name: "Figgis"}
//                           }
//                       ]
//                   }



}

