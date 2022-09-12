
var navSearchInput = $('#navSearchInput');
var navSearchForm = $('#navSearchForm');
var navSearchSelectButton = $('#navSearchSelectButton');

function setNavSearch(action, name, caller){
    navSearchForm.attr("action", action);
    navSearchInput.attr("name", name);
    navSearchSelectButton.text(caller.textContent);
}

function setCheckboxState(checkbox, checked = false){
    var check = checkbox.prop('checked');
    if(checked) {
        $('.checker').find('span').addClass('checked');
        $('.checkbox').prop('checked', true);
    } else {
        $('.checker').find('span').removeClass('checked');
        $('.checkbox').prop('checked', false);
    }
}
