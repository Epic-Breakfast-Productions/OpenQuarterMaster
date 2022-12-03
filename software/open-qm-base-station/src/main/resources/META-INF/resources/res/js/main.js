
var navSearchInput = $('#navSearchInput');
var navSearchForm = $('#navSearchForm');
var navSearchTypeSelect = $('#navSearchTypeSelect');

navSearchTypeSelect.on("change", function(event){
    console.log(
        "Changing nav form to " +
        "action: " + navSearchTypeSelect[0].options[event.target.selectedIndex].dataset.action +
        " with fieldName: " + navSearchTypeSelect[0].options[event.target.selectedIndex].dataset.field
    );
    navSearchForm.attr("action", navSearchTypeSelect[0].options[event.target.selectedIndex].dataset.action);
    navSearchInput.attr("name", navSearchTypeSelect[0].options[event.target.selectedIndex].dataset.field);
});

//think this can be removed but not 100% sure
// function setCheckboxState(checkbox, checked = false){
//     var check = checkbox.prop('checked');
//     if(checked) {
//         $('.checker').find('span').addClass('checked');
//         $('.checkbox').prop('checked', true);
//     } else {
//         $('.checker').find('span').removeClass('checked');
//         $('.checkbox').prop('checked', false);
//     }
// }
