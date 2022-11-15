'use strict'

const path = require('path')
const fs = require('fs')
const appDirectory = fs.realpathSync(process.cwd())
const resolveApp = (relativePath) => path.resolve(appDirectory, relativePath)
const getPublicUrl = appPackageJson =>
  require(appPackageJson).homepage;
const namespace = {
  appRoot: path.resolve('src'),
  appAssets: path.resolve('src/assets'),
}
module.exports = {
  dotenv: resolveApp('.env'),
  appBuild: resolveApp('build'),
  appPublic: resolveApp('public'),
  appHtml: resolveApp('public/index.html'),
  staticGizped: resolveApp('build/static'),
  appIndexJs: resolveApp('src/index.js'),
  appPackageJson: resolveApp('package.json'),
  appSrc: resolveApp('src'),
  yarnLockFile: resolveApp('yarn.lock'),
  testsSetup: resolveApp('src/setupTests.js'),
  appNodeModules: resolveApp('node_modules'),
  packConfig: resolveApp('config'),
  publicUrl: getPublicUrl(resolveApp('package.json')),
  appRoot: path.resolve('src'),
  namespace: namespace,
}
