(function () {
  // Close sidebar if it already exists
  const existingHost = document.getElementById(
    "ext-sidebar-host"
  );

  if (existingHost) {
    existingHost.remove();
    return;
  }

  createSidebar();
})();