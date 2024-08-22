# Developing Depot

## Testing

`docker build -t ebprod/oqm-core-depot . && docker run -p 8085:80 ebprod/oqm-core-depot`

## Deployment

`docker buildx build . -t ebprod/oqm-core-depot:$(jq -r '.version' webroot/composer.json) --platform linux/amd64,linux/arm64 --push`

old:

`docker build -t ebprod/oqm-core-depot:$(jq -r '.version' webroot/composer.json) . && docker push ebprod/oqm-core-depot:$(jq -r '.version' webroot/composer.json)`
