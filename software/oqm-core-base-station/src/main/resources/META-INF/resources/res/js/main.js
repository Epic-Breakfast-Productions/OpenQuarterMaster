const navSearchInput = $('#navSearchInput');
const navSearchForm = $('#navSearchForm');
const navSearchTypeSelect = $('#navSearchTypeSelect');

function updateNavSearchDestination(action, icon, fieldName){
    navSearchForm.attr("action", action);
    navSearchTypeSelect.html(icon);
    navSearchInput.attr("name", fieldName);
}

const Main = {
    /**
     *
     * Initial value for processes:
     *  - page messages
     *  - time helpers
     *  - this popovers
     *  - dselect
     */
    processCount: 4,
    processStart(){
        this.processCount++;
    },
    processStop(){
        this.processCount--;
    },
    processesRunning(){
        return this.processCount !== 0;
    },
    noProcessesRunning(){
        return !this.processesRunning();
    }
}

console.log("===== New Page Loading =====");
let popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
let popoverList = popoverTriggerList.map(function (popoverTriggerEl) {
    return new bootstrap.Popover(popoverTriggerEl)
});
Main.processStop();