const webpack = require('webpack');
const path = require('path');
const paths = require('./paths');
const runtimePaths = require('./runtimePaths');
const CopyWebpackPlugin = require('copy-webpack-plugin');

module.exports = {
    entry: {
        vendor: ['bizcharts'],
    },
    output: {
        filename: '[name].dll.js',
        path: paths.appBuild,
        library: "[name]_[hash]"
    },
    plugins: [
        new CopyWebpackPlugin([
            ...runtimePaths.dependency_arr_pre
        ]),
    ]
};