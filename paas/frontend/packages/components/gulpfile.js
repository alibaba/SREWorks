const less = require('gulp-less')
const postcss = require('gulp-postcss')
const cleanCSS = require('gulp-clean-css')
const path = require('path')
const rimraf = require('rimraf')
const merge2 = require('merge2')
const gulp = require('gulp')

function compile(module) {
  const dir = module ? './es' : './lib'
  // rimraf.sync(dir)
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

  const lessToLib = gulp.src('./src/**/*.less').pipe(gulp.dest(dir))
  return [lessToCss, lessToLib]
}

gulp.task('default', function () {
  return merge2(...compile(), ...compile(true))
})
