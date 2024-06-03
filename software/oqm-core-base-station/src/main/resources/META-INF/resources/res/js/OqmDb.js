const OqmDbUtils = {
    navDatabaseSelector: $("#navDatabaseSelector"),
    newDbSelected: function (){
        let newDbId = this.navDatabaseSelector.value();
        console.log("Input to select database changed. New selection: ", newDbId);

        if(confirm("Are you sure you want to swap databases?")){
            console.log("User confirmed database switch.");
            //TODO:: set cookie
            PageMessages.reloadPageWithMessage("Successfully swapped databases.", "success");
        } else {
            console.log("User canceled db swap.");
        }
    }
}