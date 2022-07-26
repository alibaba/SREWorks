const fs = require('fs-extra');
const path = require('path');
const paths = require('./paths')
const getPackageJson = () => {
    var _packageJson = fs.readFileSync('./package.json')
    return JSON.parse(_packageJson)
}
const getDependencyVesion = (packagejson, dependency) => {
    if (!packagejson || !dependency) {
        return ''
    }
    var initVesrion = '';
    var dependencies = packagejson['dependencies']
    initVesrion = dependencies[dependency];
    if (initVesrion.startsWith('^') || initVesrion.startsWith("~")) {
        return initVesrion.slice(1);
    }
    return initVesrion
}

var packagejson = getPackageJson();
var antdPath = getDependencyVesion(packagejson, 'antd');
var momentPath = getDependencyVesion(packagejson, 'moment');
var reactPath = getDependencyVesion(packagejson, 'react');
var react_dom_path = getDependencyVesion(packagejson, 'react-dom');
var systemjsPath = getDependencyVesion(packagejson, 'systemjs');
var bizchartsPath = getDependencyVesion(packagejson, 'bizcharts');

const dependency_arr = [{
    from: paths.appNodeModules + '/antd/dist/antd.min.js',
    to: paths.appBuild + '/common_vendor/antd/' + antdPath + '/antd.min.js'
},
{
    from: paths.appNodeModules + '/react/umd/react.production.min.js',
    to: paths.appBuild + '/common_vendor/react/' + reactPath + '/react.production.min.js'
},
{
    from: paths.appNodeModules + '/react-dom/umd/react-dom.production.min.js',
    to: paths.appBuild + '/common_vendor/react-dom/' + react_dom_path + '/react-dom.production.min.js'
},
{
    from: paths.appNodeModules + '/moment/min/moment.min.js',
    to: paths.appBuild + '/common_vendor/moment/' + momentPath + '/moment.min.js'
},
{
    from: paths.appNodeModules + '/systemjs/dist/system.min.js',
    to: paths.appBuild + '/common_vendor/systemjs/' + systemjsPath + '/system.min.js'
},
{
    from: paths.appNodeModules + '/bizcharts/umd/BizCharts.min.js',
    to: paths.appBuild + '/common_vendor/bizcharts/' + bizchartsPath + '/BizCharts.min.js'
}]
console.log("init runtime paths successfull")

module.exports = {
    antdPath,
    momentPath,
    reactPath,
    react_dom_path,
    systemjsPath,
    bizchartsPath,
    dependency_arr
}
