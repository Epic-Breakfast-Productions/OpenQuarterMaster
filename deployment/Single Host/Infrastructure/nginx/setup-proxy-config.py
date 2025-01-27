#!/bin/python3
#
# Setup Proxy Config Script
# This script sets up the nginx proxy configuration file for Open QuarterMaster.
#

import logging
import os
import json
import sys
from html.parser import HTMLParser
sys.path.append("/usr/lib/oqm/station-captain/")
from ConfigManager import *
from LogUtils import *
LogUtils.setupLogging("infra-nginx-proxy-config.log", "--verbose" in sys.argv)

log = LogUtils.setupLogger("main")
log.info("==== STARTING NGINX CONFIG GENERATION ====")
log.info("Adding proxy config to nginx config.")

CONFIG_TEMPLATE_FILE = "/etc/oqm/serviceConfig/infra/nginx/default.conf.template"
PROXY_CONFIG_DIR = "/etc/oqm/proxyConfig.d"
RESULT_CONFIG_FILE = "/tmp/oqm/serviceConfig/infra/nginx/config.d/default.conf"

INDEX_TEMPLATE_FILE = "/etc/oqm/serviceConfig/infra/nginx/indexTemplate.html"
INDEX_FILE = "/etc/oqm/serviceConfig/infra/nginx/webroot/index.html"


upstreamText = "\n"
locationText = "\n"
redirect = ""

coreLinks = ""
infraLinks = ""

for curProxyConfigFile in os.listdir(PROXY_CONFIG_DIR):
    if curProxyConfigFile.endswith(".json"):
        log.info("Cur proxy file: " + curProxyConfigFile)
        with open(PROXY_CONFIG_DIR + "/" + curProxyConfigFile, 'r') as stream:
            curProxyConfig = json.load(stream)

        curProxyConfig["path"] = "/" + curProxyConfig["type"] + "/" + curProxyConfig["name"]

        if "host" not in curProxyConfig:
            curProxyConfig["host"] = mainCM.getConfigVal(curProxyConfig["hostConfig"])
        if "port" not in curProxyConfig:
            curProxyConfig["port"] = mainCM.getConfigVal(curProxyConfig["portConfig"])

        curProxyConfig["upstreamName"] = curProxyConfig["host"]

        log.info("Using proxy config: %s", curProxyConfig)

        if curProxyConfig["type"] == mainCM.getConfigVal("infra.nginx.defaultUi.type") and curProxyConfig["name"] == mainCM.getConfigVal("infra.nginx.defaultUi.name"):
            redirect = ("""
<script>
	window.location.replace("{landingPage}");
</script>
""".format(landingPage=mainCM.getConfigVal("infra.nginx.externalRootUri")+curProxyConfig["path"]))

        upstreamText += ("""
upstream {upstreamName} {{
    server {upstreamHost}:{upstreamPort};
}}

        """.format(
            upstreamName=curProxyConfig["upstreamName"],
            upstreamHost=curProxyConfig["host"],
            upstreamPort=curProxyConfig["port"]
        ))

        locationText += """
    location {path} {{
        resolver 127.0.0.11 valid=1s; # https://stackoverflow.com/questions/32845674/nginx-how-to-not-exit-if-host-not-found-in-upstream
        #set $dockerHost {upstreamName};
        
        # TODO:: determine if https or not
        proxy_pass https://{upstreamName};
        #proxy_redirect http://{upstreamName}/ /;
        
        proxy_buffering off;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Port $server_port;
        
        #proxy_redirect off;
        
        
        # https://docs.nginx.com/nginx/admin-guide/security-controls/securing-http-traffic-upstream/
        proxy_ssl_verify       off;
        #proxy_ssl_certificate     /etc/nginx/client.pem;
        #proxy_ssl_certificate_key /etc/nginx/client.key;

        
        
        
        

        # tell backend that message compression not allowed
        # proxy_set_header Accept-Encoding "";

        # sub_filter_types *;
        # sub_filter 'action="/'  'action="{path}';
        # sub_filter 'href="/'  'href="{path}';
        # sub_filter 'src="/'  'src="{path}';
        # sub_filter 'action="./'  'action="{path}';
        # sub_filter 'href="./'  'href="{path}';
        # sub_filter 'src="./'  'src="{path}';
        # sub_filter_once off;

        # add_header Pragma "no-cache";
        # add_header Cache-Control "no-cache";

        # include  /etc/nginx/mime.types;
        # include /etc/nginx/conf.other.d/proxy_params;
    }}

""".format(
            path=curProxyConfig["path"],
            upstreamName=curProxyConfig["upstreamName"]
        )
    else:
        continue

log.info("Done reading in proxy config files.")

with open(CONFIG_TEMPLATE_FILE, "r") as f:
    configData = f.read()
configData = configData.format(
    # upstreamText=upstreamText,
    upstreamText="\n",
    locationText=locationText,
    hostHost=mainCM.getConfigVal("system.hostname")
)
os.makedirs(os.path.dirname(RESULT_CONFIG_FILE), exist_ok=True)
with open(RESULT_CONFIG_FILE, "w") as f:
    f.write(configData)
log.info("Finished writing new config file.")

with open(INDEX_TEMPLATE_FILE, "r") as f:
    index = f.read()
index = index.format(redirect=redirect)
os.makedirs(os.path.dirname(INDEX_FILE), exist_ok=True)
with open(INDEX_FILE, "w") as f:
    f.write(index)
log.info("Finished writing new index file.")


log.info("==== END OF NGINX CONFIG GENERATION ====")
