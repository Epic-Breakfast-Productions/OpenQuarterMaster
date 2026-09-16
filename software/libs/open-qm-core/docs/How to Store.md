# How to Store

Guide on how stored items are represented in the system.

## Pounds of dirt

Amount Stored

```json
{
  "name": "Dirt",
  "unit": "pounds",
  "storageMap": {
    "<blockId>": [
      {
        "amount": "10 pounds"
      }
    ]
  }
}
```

## Gallon Jugs of Milk

Amount Stored

```json
{
  "name": "Gallon 2% Milk",
  "unit": "one",
  "atts": {
    "sku": "12345"
  },
  "storageMap": {
    "<blockId>": [
      {
        "amount": "10 one",
        "expires": "next week"
      },
      {
        "amount": "2 one",
        "expires": "tomorrow"
      }
    ]
  }
}
```

## Desktop Computer

Tracked Item

```json
{
  "name": "Dell Tower Opplex 420",
  "unit": "one",
  "itemIdentifier": "serial #",
  "storageMap": {
    "<blockId>": {
      "<S/N>": {
      }
    }
  }
}
```



