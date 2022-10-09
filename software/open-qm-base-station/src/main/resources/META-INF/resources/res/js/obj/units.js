
var unitOptions = $("#unitSelectOptions").children();

function getUnitOptions(selectedVal){
    var output = unitOptions.clone();

//    if(selectedVal){
//        output.each(function(i, curOptGrp){
//            $(curOptGrp).children().each(function(j, curOption){
//                if(curOption.value == selectedVal){
//                    curOption.attributes["selected"] = true;
//                }
//            });
//        });
//    }
    return output;
}

function updateCompatibleUnits(unitToCompatWith, containerToSearch){
    return doRestCall({
        url: "/api/info/unitCompatibility/" + unitToCompatWith,
        extraHeaders: { accept:"text/html" },
        done: function (data){
            compatibleUnitOptions = data;

            containerToSearch.find(".unitInput").each(function (i, selectInput){
                var selectInputJq = $(selectInput);
                selectInputJq.html(compatibleUnitOptions);
                selectInputJq.change();
            });
        }
    });
}

function getQuantityObj(value, unit){
    let output = {
        unit: {
            string: unit
        },
        scale: "ABSOLUTE",
        value: value
    };

    return output;
}