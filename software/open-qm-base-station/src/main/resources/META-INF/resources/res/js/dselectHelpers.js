
const Dselect = {
  standardOps: { search: true, clearable: true },
  setupDselect(selectObj){
    dselect(selectObj, Dselect.standardOps);
  },
  setupPageDselects(){
    document.querySelectorAll(".dselect-select").forEach(function(cur){
      try {
        Dselect.setupDselect(cur);
      } catch (error) {
        console.error(error);
      }
    })
  }
};