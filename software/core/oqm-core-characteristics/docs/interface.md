# OQM Characteristics Service Interface

## All

`/all` will return all of the following datas, like:

```json
{
   "characteristics" : {
      // data from /characteristics
   },
   "uis": {
      // data from /uis
   }
}
```

## Characteristics

The endpoint to get characteristics data is:

`/characteristics`

Format:

```json
{
  "title": null,
  "motd": null,
  "runBy": {
    "name": null,
    "email": null,
    "phone": null,
    "website": null,
    "hasLogoImg": false,
    "hasBannerImg": false
  },
  "banner": { //this can be null if no values
    "text": null,
    "textColor": null,
    "backgroundColor": null
  }
}
```

These values match 1:1 in the characteristics file data outlined in [config](./configuration.md).

### Images

To actually retrieve image data (if available), use

`/characteristics/logo`

and

`/characteristics/banner`

(if no image was available, these endpoints will return with a `400` error)


## UIs

These values are a list of ui's available on the system. This data is used to infer where else the user can go.

Endpoint: `/uis`

Format:

```json
{
   "home": "",// homepage of the system
   "core": [
      // UI entries
   ],
   "plugin": [
      // UI entries
   ],
   "metrics": [
      // UI entries
   ],
   "infra": [
      // UI entries
   ]
}
```

Where a single UI entry is described as:

```json
{
    "name": "",
    "id": "",
    "description": "",
    "baseUri": "",
    "icon": true,
    "endpoints": {
        "health": "/q/health",
        "item": {
           "view": "/items?item={item}"
        }
    }
}
```

### Icons

To actually retrieve image data (if available), use

`/uis/{category}/{id}/icon`

Where `category` is the type of ui, like `plugin`, and `id` is the id of the entry in the array of results.

(if no image was available, these endpoints will return with a `400` error)
