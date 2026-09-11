(function () {
  // This file runs fresh every time the toolbar icon is clicked on this tab.
  // If the sidebar is already present, treat this click as "close it".
  const existingHost = document.getElementById("ext-sidebar-host");
  if (existingHost) {
    existingHost.remove();
    return;
  }

  const host = document.createElement("div");
  host.id = "ext-sidebar-host";
  document.documentElement.appendChild(host);

  // Shadow DOM keeps our CSS from leaking into (or being clobbered by) the page
  const shadow = host.attachShadow({ mode: "open" });

  shadow.innerHTML = `
    <style>
      :host { all: initial; }

      .sidebar {
        position: fixed;
        top: 0;
        right: 0;
        height: 100vh;
        width: 220px;
        background: #ffffff;
        border-left: 1px solid #d0d0d0;
        box-shadow: -2px 0 6px rgba(0, 0, 0, 0.08);
        z-index: 2147483647; /* stay above page content */
        font-family: system-ui, -apple-system, sans-serif;
        transition: width 0.15s ease;
        box-sizing: border-box;
      }

      .sidebar.collapsed {
        width: 16px;
        border-left: 1px solid #d0d0d0;
        box-shadow: none;
      }

      .toggle-btn {
        position: absolute;
        top: 12px;
        left: -14px;
        width: 28px;
        height: 28px;
        border-radius: 50%;
        border: 1px solid #d0d0d0;
        background: #ffffff;
        cursor: pointer;
        font-size: 13px;
        line-height: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 0;
      }

      .toggle-btn:hover {
        background: #f0f0f0;
      }

      .content {
        padding: 40px 12px 12px 12px;
        display: flex;
        flex-direction: column;
        gap: 8px;
      }

      .sidebar.collapsed .content {
        display: none;
      }

      button.action {
        padding: 8px 10px;
        font-size: 13px;
        border: 1px solid #d0d0d0;
        border-radius: 4px;
        background: #f7f7f7;
        cursor: pointer;
        text-align: left;
        color: #222;
        font-family: inherit;
      }

      button.action:hover:not(:disabled) {
        background: #ededed;
      }

      button.action:disabled {
        color: #a0a0a0;
        cursor: not-allowed;
        background: #f2f2f2;
      }
    </style>

    <div class="sidebar" id="sidebar">
      <button class="toggle-btn" id="toggle-btn" title="Collapse">&rsaquo;</button>
      <div class="content">
        <button class="action" id="scan-btn">Scan Page</button>
        <button class="action" id="feedback-btn">Feedback</button>
        <button class="action" id="integrate-btn" disabled>Request integration with this site</button>
      </div>
    </div>
  `;

  const sidebar = shadow.getElementById("sidebar");
  const toggleBtn = shadow.getElementById("toggle-btn");

  toggleBtn.addEventListener("click", () => {
    const collapsed = sidebar.classList.toggle("collapsed");
    toggleBtn.innerHTML = collapsed ? "&lsaquo;" : "&rsaquo;";
    toggleBtn.title = collapsed ? "Expand" : "Collapse";
  });

  shadow.getElementById("scan-btn").addEventListener("click", () => {
    console.log("Scan Page clicked");
  });

  shadow.getElementById("feedback-btn").addEventListener("click", () => {
    console.log("Feedback clicked");
  });
})();