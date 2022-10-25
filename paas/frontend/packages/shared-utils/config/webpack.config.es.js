const { merge } = require('webpack-merge')
const path = require('path')
const baseConfig = require('./webpack.config.base')

module.exports = merge(baseConfig, {
  output: {
    filename: '[name].js',
    path: path.resolve(process.cwd(), 'es'),
    library: {
      type: 'module',
    },
  },
  experiments: {
    outputModule: true,
  },
})
