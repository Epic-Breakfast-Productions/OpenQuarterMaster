chrome.action.onClicked.addListener((tab) => { //only for the active tab
  chrome.scripting.executeScript({
    target: { tabId: tab.id },
    files: ["content.js"],
  });
});