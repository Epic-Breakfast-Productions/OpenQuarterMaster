
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