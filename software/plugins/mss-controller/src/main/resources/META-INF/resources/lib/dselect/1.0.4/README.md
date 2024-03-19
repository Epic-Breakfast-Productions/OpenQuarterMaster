# dselect

Dropdown select box for bootstrap 5

[Demo](https://dselect.vercel.app)

## Features

- Placeholder
- Multiple
- Search
- Creatable
- Clearable
- Sizing
- Validation


## Installation

Install dselect with npm

```bash
npm install @jarstone/dselect
```

Install from cdn
```html
<link rel="stylesheet" href="https://unpkg.com/@jarstone/dselect/dist/css/dselect.css">
<script src="https://unpkg.com/@jarstone/dselect/dist/js/dselect.js"></script>
```
## Usage/Examples

```html
<select class="form-select" id="dselect-example">
    <option value="chrome">Chrome</option>
    <option value="firefox">Firefox</option>
    <option value="safari">Safari</option>
    <option value="edge">Edge</option>
    <option value="opera">Opera</option>
    <option value="brave">Brave</option>
</select>
```
```javascript
dselect(document.querySelector('#dselect-example'))
```
## Options

```javascript
const config = {
    search: false, // Toggle search feature. Default: false
    creatable: false, // Creatable selection. Default: false
    clearable: false, // Clearable selection. Default: false
    maxHeight: '360px', // Max height for showing scrollbar. Default: 360px
    size: '', // Can be "sm" or "lg". Default ''
}
dselect(document.querySelector('#dselect-example'), config)
```

options can also be set in "data-dselect-*" attribute

```html
<select data-dselect-search="true" data-dselect-creatable="true" data-dselect-clearable="true" data-dselect-max-height="300px" data-dselect-size="sm" class="form-select" id="dselect-example">
...
</select>
```
