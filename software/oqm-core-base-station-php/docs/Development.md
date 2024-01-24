# Developing Depot

## Testing

`docker build -t ebprod/oqm-core-base-station-test . && docker run -p 8085:80 ebprod/oqm-core-base-station-test`

## Deployment

`docker build -t ebprod/oqm-core-base-station:$(jq -r '.version' webroot/composer.json) . && docker push ebprod/oqm-core-base-station:$(jq -r '.version' webroot/composer.json)`
