

# OQM Browser Extension

OQM Shopping Cart Integrator
│
├── manifest.json
│
├── frontend/
│   ├── content.js
│   ├── sidebar.js
│   └── sidebar.css
│
└── services/
    ├── background.js
    ├── auth.js
    ├── scanner.js
    └── integration.js
-------------------------------------------------------------------------------------
scanner.js
    scanPage()
        ↓
    returns cartData

auth.js
    login()
    logout()
    isLoggedIn()

integration.js
    requestIntegration(cartData)
