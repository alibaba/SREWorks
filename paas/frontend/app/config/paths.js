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
  '@ant-design': path.resolve(process.cwd(), 'node_modules', '@ant-design'),
  'js-yaml': path.resolve(process.cwd(), 'node_modules', 'js-yaml'),
  'ace-builds': path.resolve(process.cwd(), 'node_modules', 'ace-builds'),
  'brace': path.resolve(process.cwd(), 'node_modules', 'brace'),
  'lodash': path.resolve(process.cwd(), 'node_modules', 'lodash')
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
  sreworksFramework: resolveApp('node_modules/@sreworks/framework'),
  sreworksComponents: resolveApp('node_modules/@sreworks/components'),
  sreworksWidgets: resolveApp('node_modules/@sreworks/widgets'),
  sreworksSharedutils: resolveApp('node_modules/@sreworks/shared-utils'),
  packConfig: resolveApp('config'),
  publicUrl: getPublicUrl(resolveApp('package.json')),
  appRoot: path.resolve('src'),
  namespace: namespace,
}
