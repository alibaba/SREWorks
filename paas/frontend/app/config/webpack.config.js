/*
 * @version: 2.0.0
 * @Author: deeham.ww
 * @Date: 2022-11-16 11:32:36
 * @LastEditors: Please set LastEditors
 * @LastEditTime: 2022-12-10 15:56:57
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
const CompressionPlugin = require('compression-webpack-plugin')
const CssMinimizerPlugin = require('css-minimizer-webpack-plugin')
const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin

const { NODE_ENV } = process.env
const DEV = NODE_ENV === 'development'
module.exports = {
  mode: DEV ? 'development' : 'production',
  devtool: DEV ? 'source-map' : false,
  entry: {
    index: paths.appIndexJs,
    ven_ant: ['@ant-design/compatible','@ant-design/icons','brace','ace-builds'],
  },
  output: {
    path: path.join(__dirname, '../build'),
    filename: 'static/js/[name].[chunkhash:8].js',
    chunkFilename: 'static/js/[name].[chunkhash:8].chunk.js',
    assetModuleFilename: 'static/media/[hash:8][ext][query]',
    clean: true,
  },
  devServer: {
    host: 'localhost',
    open: true,
    hot: true,
    port: 8080,
    proxy: {
      '/gateway': {
        target: '',
        changeOrigin: true,
        cookieDomainRewrite: 'localhost',
      },
    },
  },
  // cache: {
  //   type: 'filesystem', // 使用文件缓存
  // },
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
              {
                loader: DEV ? 'style-loader' : MiniCssExtractPlugin.loader,
  
              },
              'css-loader'
            ],
          },
          {
            test: /\.less$/,
            use: [          
              {
              loader: DEV ? 'style-loader' : MiniCssExtractPlugin.loader,

            },
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
              {
                loader: DEV ? 'style-loader' : MiniCssExtractPlugin.loader,
  
              },
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
              {
                loader: DEV ? 'style-loader' : MiniCssExtractPlugin.loader,
  
              },
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
      new CssMinimizerPlugin()
    ],
    splitChunks: {
      chunks: 'all',
      minSize: 3000,
      cacheGroups:{
        vendors:{ // node_modules里的代码
          test: /[\\/]node_modules[\\/]/,
          priority: -10, // 优先级
          enforce: true 
        },
      }
    }
  },
  plugins: [
    new polyfillPlugin(),
    new HtmlWebpackPlugin({
      template: path.join(__dirname, '../public/index.html'),
      filename: 'index.html',
      inject: 'body',
    }),
    new MiniCssExtractPlugin({
      filename:'static/css/[name].css',
      chunkFilename: 'staic/css/[name].chunk.css',
      ignoreOrder: true
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
      {
        from: paths.appSrc + '/assets/icons',
        to: paths.appBuild + '/static/icons'
    }
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
  //  !DEV && new CompressionPlugin({
  //   filename: "[path][base].gz",
  //   exclude: [        
  //     path.resolve(__dirname, 'vendors'),             
  //   ],
  //   algorithm: "gzip",
  //   test: /\.(js|css|png|svg|jpg)$/,
  //   threshold: 10240,// 大于10kb的才被压缩
  //   minRatio: 0.8,//压缩比例
  //   deleteOriginalAssets: true,
  // }),
  // new BundleAnalyzerPlugin(),
  ].filter(Boolean),
}
