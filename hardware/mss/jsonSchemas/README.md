# Json Schemas for MSS

This directory contains the json schemas that outline how communication with Modular Storage System (MSS) modules
operates.

## How it works

Generally speaking, the actual MSS Modules are rather 'dumb'. They only provide basic information on the storage space
available, and take commands to do specific things, like mark specific storage blocks for easy finds.

## Commands

### Get Module Info

### Get Module State

### Get Block State

### Highlight Blocks

array of which blocks, solid or flashing, duration

## Sources

 - https://json-schema.org/understanding-json-schema/index.html
 - https://github.com/json-schema-org/json-schema-spec/issues/348
 - https://mkyong.com/regular-expressions/how-to-validate-hex-color-code-with-regular-expression/
 - https://json-schema.org/understanding-json-schema/structuring.html
 - https://json-schema.org/understanding-json-schema/reference/array.html
 - 