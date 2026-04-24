# BarcodeLookup

 - https://www.barcodelookup.com/api
 - https://www.barcodelookup.com/api-documentation

## Examples

Barcode:

`curl -H "Accept: application/json" "https://api.barcodelookup.com/v3/products?barcode=9780140157376&formatted=y&key={key}" | less`

Query search:

`curl -H "Accept: application/json" "https://api.barcodelookup.com/v3/products?search=GPS&formatted=y&key={key}"`
