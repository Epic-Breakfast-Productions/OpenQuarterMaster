
//TODO:: move others into here
const UnitUtils = {

    /**
     * Example input: {"unit":{"string":"units","name":"Units","symbol":"units"},"scale":"ABSOLUTE","value":8}
     * @param quantityObj
     */
    quantityToDisplayStr(quantityObj){
        return quantityObj.value + quantityObj.unit.symbol;
    }
};


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

//TODO:: review usage of ItemAddEdit.compatibleUnitOptions, and where it/this function should live
function updateCompatibleUnits(unitToCompatWith, containerToSearch){
    doRestCall({
        url: "/api/v1/info/unitCompatibility/" + unitToCompatWith,
        extraHeaders: { accept:"text/html" },
        async: false,
        done: function (data){
            ItemAddEdit.compatibleUnitOptions = data;

            containerToSearch.find(".unitInput").each(function (i, selectInput){
                var selectInputJq = $(selectInput);
                selectInputJq.html(ItemAddEdit.compatibleUnitOptions);
                selectInputJq.change();
            });
        }
    });
}

function getUnitObj(unitStr){
    let output = {
            string: unitStr
        };

    return output;
}

function getQuantityObj(value, unit){
    let output = {
        unit: getUnitObj(unit),
        scale: "ABSOLUTE",
        value: value
    };

    return output;
}