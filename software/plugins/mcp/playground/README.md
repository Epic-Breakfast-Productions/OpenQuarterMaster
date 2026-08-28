# OQM MCP Server Playground

Tested with `granite4.2:3b`


References:

 - Ollmcl Github: https://github.com/jonigl/mcp-client-for-ollama

## Requirements

Ollama, with tool-capable model setup.

`oqm-plugin-mcp` (this project) running in dev mode

## Setup

Setup virtual environment:

```bash
python3 -m venv ./.venv
source ./.venv/bin/activate
pip install -r requirements.txt
```

Add the dev instance to ollmcp configuration:

```bash
ollmcp mcp add --transport http oqm-dev-mcp http://localhost:8080/mcp
```

## Running

 1. Make sure the OQM MCP service is running in dev mode.
 2. run `ollmcp`
    - You should see a green banner showing that the tool has connected to the MCP server
 3. Enter a prompt like "How many storage blocks do I have?"
