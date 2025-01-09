const { src, dest, watch, series, parallel } = require('gulp')
const fs       = require('fs-extra')
const util     = require('util')
const path     = require('path')
const exec     = util.promisify(require('child_process').exec)
const rename   = require('gulp-rename')
const esbuild  = require('esbuild')
const through  = require('through2')

const source = {
  css : 'source/css',
  js  : 'source/js',
}

const compiled = {
  css : 'dist/css',
  js  : 'dist/js',
}

function clean() {
  return new Promise(function (resolve) {
    for (const [key, value] of Object.entries(compiled)) {
      fs.emptyDirSync(value)
    }
    resolve()
  })
}

let filtered_css = []
function css() {
  return new Promise(async function (resolve) {
    if (filtered_css.length) {
      for (const file of filtered_css) {
        const dest = file.replace(
          path.join(__dirname, source.css),
          path.join(__dirname, compiled.css)
        ).slice(0, -4) + 'css'
        await exec(`sass --source-map --embed-sources ${file} ${dest} --quiet`).catch(err => console.log(err.stderr))
      }
    } else {
      await exec(`sass --source-map --embed-sources ${source.css}:${compiled.css} --quiet`).catch(err => console.log(err.stderr))
    }
    resolve()
  })
}

function css_watch() {
  watch(source.css).on('all', async function (event, target) {
    const obj = path.parse(target)
    let targets = []
    switch (event) {
      case 'add':
      case 'change':
        if (obj.name.startsWith('_')) {
          await new Promise(async function (resolve) {
            src(`${source.css}/**/!(_*).scss`)
              .pipe(through.obj(function (file, enc, callback) {
                const content = file.contents.toString().split(/\r?\n/).filter(i => i.startsWith('@import')).join('')
                if (content.includes(`${obj.name.substring(1)}'`) || content.includes(`${obj.name.substring(1)}"`)) {
                  targets.push(file.path)
                }
                return callback()
              }))
              .on('finish', resolve)
          })
        } else if (obj.ext === '.scss') {
          targets.push(path.join(__dirname, obj.dir, obj.base))
        }
        break;
      case 'unlink':
        const removedTarget = path.join(__dirname, target).replace(
          path.join(__dirname, source.css),
          path.join(__dirname, compiled.css),
        ).slice(0, -4) + 'css'
        fs.removeSync(removedTarget)
        fs.removeSync(removedTarget + '.map')
        fs.removeSync(removedTarget.slice(0, -3) + 'min.css')
        break;
    }
    filtered_css = targets
  })
  return watch(source.css, css)
}

function css_prefix() {
  return new Promise(async function (resolve) {
    await exec(`npx postcss ${compiled.css}/*.css !${compiled.css}/*.min.css --use autoprefixer --map --replace`).catch(err => console.log(err.stderr))
    resolve()
  })
}

function css_minify() {
  return src(`${compiled.css}/!(*.min).css`)
    .pipe(through.obj(function (file, enc, callback) {
      let content = file.contents.toString()
      content = esbuild.transformSync(content, {
        loader: 'css',
        minify: true,
      }).code
      file.contents = Buffer.from(content)
      this.push(file)
      return callback()
    }))
    .pipe(rename({ suffix: '.min' }))
    .pipe(dest(compiled.css))
}

let filtered_js = []
function js() {
  return src(`${source.js}/**/*.js`)
    .pipe(through.obj(function (file, enc, callback) {
      if (pass(file.path, filtered_js)) {
        let content   = file.contents.toString()
        content       = esbuild.transformSync(content).code
        file.contents = Buffer.from(content)
        this.push(file)
      }
      return callback()
    }))
    .pipe(dest(compiled.js))
}

function js_watch() {
  watch(source.js).on('all', async function (event, target) {
    const obj = path.parse(target)
    let targets = []
    switch (event) {
      case 'add':
      case 'change':
        if (obj.ext === '.js') {
          targets.push(path.join(__dirname, obj.dir, obj.base))
        }
        break;
      case 'unlink':
        const removedTarget = path.join(__dirname, target).replace(
          path.join(__dirname, source.js),
          path.join(__dirname, compiled.js),
        )
        fs.removeSync(removedTarget)
        fs.removeSync(removedTarget.slice(0, -2) + 'min.js')
        break;
    }
    filtered_js = targets
  })
  return watch(source.js, js)
}

function js_minify() {
  return src(`${compiled.js}/!(*.min).js`)
    .pipe(through.obj(function (file, enc, callback) {
      let content = file.contents.toString()
      content = esbuild.transformSync(content, {
        minify: true,
      }).code
      file.contents = Buffer.from(content)
      this.push(file)
      return callback()
    }))
    .pipe(rename({ suffix: '.min' }))
    .pipe(dest(compiled.js))
}

function pass(item, items) {
  if (items.length === 0) {
    return true
  } else {
    return items.includes(item)
  }
}

exports.css        = css
exports.css_watch  = css_watch
exports.css_prefix = css_prefix
exports.css_minify = css_minify
exports.js         = js
exports.js_watch   = js_watch
exports.js_minify  = js_minify

exports.dev = parallel(css_watch, js_watch)
exports.build = series(
  clean,
  parallel(
    series(
      css,
      css_prefix,
      css_minify,
    ),
    series(
      js,
      js_minify,
    ),
  )
)