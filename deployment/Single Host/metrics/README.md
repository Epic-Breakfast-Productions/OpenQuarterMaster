# Metrics components for Single Host OQM Deployment


## Resources

 - LGTM Git: https://github.com/grafana/docker-otel-lgtm
   - Discussion on disabling anonymous: https://github.com/grafana/docker-otel-lgtm/discussions/354
   - Discussion on preloading dashboards/ configuration: https://github.com/grafana/docker-otel-lgtm/discussions/1106
 - Grafana keycloak setup: https://grafana.com/docs/grafana/latest/setup-grafana/configure-access/configure-authentication/keycloak/

## TOODs

 - [ ] make installer
 - [ ] selectively include exposed metrics ui's, from config
 - [ ] make sure can run behind proxy
 - [ ] add oidc to grafana, only see when logged in?
 - [ ] add metrics back to core api, base station. Verify connection
 - [ ] Load dashboards.. create dashboards?
   - [ ] System resource metrics
   - [ ] OQM inventory metrics