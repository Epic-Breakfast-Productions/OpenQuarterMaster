// https://fperucic.github.io/treant-js/

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

    if(curBlock.firstImageId){
        curNode.image = "/api/media/image/"+curBlock.firstImageId+"/data";
    }

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
}

function addCrumbs(cur, crumbList, toKeepId){
    if(cur.blockId != toKeepId){
        crumbList.append($('<li class="breadcrumb-item"><a href="#">'+cur.blockLabel+'</a></li>'));
        addCrumbs(cur.children[0], crumbList, toKeepId);
    } else {
        crumbList.append($('<li class="breadcrumb-item"active" aria-current="page">'+cur.blockLabel+' (this)</li>'));
    }
}


function getBlockBreadcrumbs(crumbContainer, toKeepId){
    doRestCall({
            url: "/api/storage/tree?onlyInclude="+toKeepId,
            done: function(data){
                console.log("Successfully got tree data.");

                var crumbList = $('<ol class="breadcrumb"></ol>');

                addCrumbs(data.rootNodes[0], crumbList, toKeepId);

                var nav = $('<nav aria-label="Storage Block Breadcrumb"></nav>');
                nav.append(crumbList);
                crumbContainer.append(nav);
            },
            fail: function(){}
    });
}