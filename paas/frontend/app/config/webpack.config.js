const path = require('path')
const HtmlWebpackPlugin = require('html-webpack-plugin')
const MiniCssExtractPlugin = require('mini-css-extract-plugin')
const TerserPlugin = require('terser-webpack-plugin')
const ESLintPlugin = require('eslint-webpack-plugin')
const webpack = require('webpack')
const paths = require('./paths')

const { NODE_ENV } = process.env
const DEV = NODE_ENV === 'development'
module.exports = {
  entry: [require.resolve('./polyfills'), paths.appIndexJs],
  // entry: './src/index.js',
  output: {
    path: path.join(__dirname, '../build'),
    filename: 'static/js/[name].[chunkhash:8].js',
    chunkFilename: 'static/js/[name].[chunkhash:8].chunk.js',
    clean: true,
  },
  devServer: {
    host: 'localhost',
    open: true,
    hot: true,
    port: 8080,
    proxy: {
      '/gateway': {
        target: 'http://dev.sreworks.net/',
        changeOrigin: true,
        cookieDomainRewrite: 'localhost',
      },
    },
  },
  mode: DEV ? 'development' : 'production',
  devtool: DEV ? 'source-map' : 'source-map',
  module: {
    rules: [
      {
        test: /\.jsx?$/,
        exclude: /node_modules/,
        loader: 'babel-loader',
      },
      {
        test: /\.css$/,
        use: ['style-loader', 'css-loader'],
      },
      {
        test: /\.less$/,
        use: [
          'style-loader',
          'css-loader',
          // less-loader
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
            loader: MiniCssExtractPlugin.loader,
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
        test: /\.(jpg|png|svg)$/i,
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
    ],
  },
  optimization: {
    minimizer: [
      new TerserPlugin({
        parallel: true,
        terserOptions: {
          output: {
            comments: false,
          },
        },
      }),
    ],
    minimize: !DEV,
    splitChunks: {
      minSize: 500000,
      chunks: 'all',
      // cacheGroups: {
      //   react: {
      //     test: /[\\/]node_modules[\\/]react(.*)?[\\/]/,
      //     name: 'chunk-react',
      //     priority: 50,
      //   },
      //   antd: {
      //     test: /[\\/]node_modules[\\/]antd(.*)?[\\/]/,
      //     name: 'chunk-antd',
      //     priority: 40,
      //   },
      // },
    },
  },
  resolve: {
    alias: paths.namespace,
    modules: ['node_modules'],
    extensions: ['.json', '.js', '.jsx', '.less', 'scss'],
  },
  plugins: [
    new HtmlWebpackPlugin({
      template: path.join(__dirname, '../public/index.html'),
      filename: 'index.html',
      inject: 'body',
    }),
    new MiniCssExtractPlugin({
      filename: '[name].css',
      chunkFilename: '[name].css',
    }),
    new ESLintPlugin(),
    new webpack.ProvidePlugin({
      process: 'process/browser',
    }),
  ].filter(Boolean),
}
