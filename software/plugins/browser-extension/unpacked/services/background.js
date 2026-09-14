chrome.action.onClicked.addListener((tab) => {
  // Only for the active tab
  chrome.scripting.executeScript({
    target: { tabId: tab.id },

    // Load service functionality first,
    // then the UI, then the entry point.
    files: [
      "services/auth.js",
      "services/scanner.js",
      "services/integration.js",
      "frontend/sidebar.js",
      "frontend/content.js"
    ]
  });
});