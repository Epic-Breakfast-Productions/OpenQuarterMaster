# Developing Depot

## Testing

`docker build -t ebprod/oqm-core-depot . && docker run -p 8085:80 ebprod/oqm-core-depot`

## Deployment

`docker build -t ebprod/oqm-core-depot:$(jq -r '.version' webroot/composer.json) . && docker push ebprod/oqm-core-depot:$(jq -r '.version' webroot/composer.json)`
