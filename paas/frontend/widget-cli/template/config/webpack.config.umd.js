/*
 * @version: 2.0.0
 * @Author: deeham.ww
 * @Date: 2022-12-27 11:32:36
 * @LastEditors: Please set LastEditors
 * @LastEditTime: 2022-12-30 14:45:34
 */
const path = require('path')
const HtmlWebpackPlugin = require('html-webpack-plugin')
const MiniCssExtractPlugin = require('mini-css-extract-plugin')
const TerserPlugin = require('terser-webpack-plugin')
const ESLintPlugin = require('eslint-webpack-plugin')
const polyfillPlugin = require('node-polyfill-webpack-plugin')
const webpack = require('webpack')
const paths = require('./paths')
const GlobalTheme = require('./globalTheme');
const copyWebpackPlugin = require('copy-webpack-plugin')
const CssMinimizerPlugin = require('css-minimizer-webpack-plugin')

const { NODE_ENV } = process.env
const DEV = NODE_ENV === 'development'
module.exports = {
  mode: DEV ? 'development' : 'production',
  devtool: DEV ? 'source-map' : false,
  entry: paths.componentsIndexJs,
  output: {
    // The build folder.
    path: path.join(__dirname, '../build'),
    // library: 'CarouselCompTest',//跟组件名保持一致
    library: "CarouselCompTest",
    libraryTarget: 'umd',
    filename: 'dist/index.umd.js',
    clean: true,
  },
  resolve: {
    alias: paths.namespace,
    modules: ['node_modules'],
    extensions: ['.json', '.js', '.jsx', '.less', 'scss'],
  },
  externals: {
    'react': 'React',
    'react-dom': 'ReactDOM',
    "antd":"antd",
    'moment':'moment',
    "moment-duration-format": "moment-duration-format",
    "systemjs": 'systemjs',
    "element-ui": "ELEMENT",
    "vue": "Vue",
    "vuera": "vuera",
    "bizcharts": "BizCharts",
    "lodash": "_",
    "html2canvas": "html2canvas",
    "jquery": "jQuery"
  },
  module: {
    rules: [
      {
        test: /\.(js|jsx|mjs)$/,
        include: paths.appSrc,
        loader: 'babel-loader',
        options: {
          compact: true,
          cacheCompression: false,
          plugins: [
            '@babel/plugin-transform-runtime'
          ]
        },
      },
      {
        oneOf:[
          {
            test: /\.css$/,
            use: [
              'style-loader',
              'css-loader'
            ],
          },
          {
            test: /\.less$/,
            use: [          
              'style-loader',
              'css-loader',
              {
                loader: 'less-loader',
                options: {
                  lessOptions: {
                    javascriptEnabled: true, // 支持js
                  },
                },
              },
            ],
          },
          {
            test: /\.(sass|scss)$/,
            use: [
              'style-loader',
              {
                loader: 'css-loader',
                options: {
                  importLoaders: 2,
                  sourceMap: !!DEV,
                },
              },
              {
                loader: 'sass-loader',
                options: {
                  sourceMap: !!DEV,
                },
              },
            ],
          },
          {
            test: /\.styl/i,
            use: [
              'style-loader',
              'css-loader',
              {
                loader: 'postcss-loader',
              },
              {
                loader: 'stylus-loader',
              },
            ],
          },
          {
            test: /\.(jpg|png|svg|jpeg|gif)$/i,
            type: 'asset/resource',
          },
          {
            test: /\.(woff|woff2|eot|ttf|otf)$/i,
            type: 'asset/resource',
          },
          {
            test: /\.(csv|tsv)$/i,
            use: ['csv-loader'],
          },
          {
            test: /\.xml$/i,
            use: ['xml-loader'],
          },
          {
            test: /\.mjs$/,
            resolve: {
              fullySpecified: false,
            },
            include: /node_modules/,
            type: 'javascript/auto',
          },
        ]
      }
      
    ],
  },
  optimization: {
    minimize: !DEV,
    minimizer: [
      new TerserPlugin({
        parallel: true,
        test: /\.js(\?.*)?$/i,
        terserOptions: {
          output: {
            comments: false,
          },
        },
      }),
    ],
  },
  plugins: [
    new polyfillPlugin(),
    new HtmlWebpackPlugin({
      template: path.join(__dirname, '../public/index.html'),
      filename: 'index.html',
      inject: 'body',
    }),
    new ESLintPlugin(),
    new webpack.ProvidePlugin({
      process: 'process/browser',
    }),
    new webpack.DefinePlugin({
      THEMES: JSON.stringify(GlobalTheme)
  }),
  new copyWebpackPlugin({
    patterns: [
      { from: 'public', to: './',globOptions: { ignore: [ "**/index.html",]}},
    ],
  }),
  new webpack.ProgressPlugin({
    activeModules: true,         
    entries: true,  			   
    modules: false,              
    modulesCount: 5000,          
    profile: false,         	   
    dependencies: false,         
    dependenciesCount: 10000,    
  }),
  ].filter(Boolean),
}
