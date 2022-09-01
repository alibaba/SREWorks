const less = require('gulp-less')
const postcss = require('gulp-postcss')
const cleanCSS = require('gulp-clean-css')
const path = require('path')
const merge2 = require('merge2')
const through2 = require('through2')
const gulp = require('gulp')

function compile(module) {
  const dir = module ? './es' : './lib'
  const lessToCss = gulp
    .src('./src/**/*.less')
    .pipe(
      less({
        paths: [path.join(__dirname, 'less', 'includes')],
      }),
    )
    .pipe(postcss())
    .pipe(cleanCSS())
    .pipe(gulp.dest(dir))

  const toJs = gulp
    .src('./src/**/style/index.js?(x)')
    .pipe(
      through2.obj(function (file, encoding, next) {
        const cloneFile = file.clone()
        if (cloneFile.path.endsWith('jsx')) {
          cloneFile.path = cloneFile.path.replace(/index\.jsx/, 'index.js')
        }
        const cloneCssFile = cloneFile.clone()
        this.push(cloneFile)
        if (cloneCssFile.path.match(/(\/|\\)style(\/|\\)index\.js/)) {
          const content = cloneCssFile.contents.toString(encoding)
          cloneCssFile.contents = Buffer.from(content.replace(/\.less/g, '.css'))
          cloneCssFile.path = cloneCssFile.path.replace(/index\.js/, 'css.js')
          this.push(cloneCssFile)
          next()
        } else {
          next()
        }
      }),
    )
    .pipe(gulp.dest(dir))

  const lessToLib = gulp.src('./src/**/*.less').pipe(gulp.dest(dir))
  const assets = gulp.src(['./src/**/*.@(png|svg)']).pipe(gulp.dest(dir))
  return [lessToCss, lessToLib, toJs, assets]
}

gulp.task('default', function () {
  return merge2(...compile(), ...compile(true))
})
