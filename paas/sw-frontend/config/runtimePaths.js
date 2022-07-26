const fs = require('fs-extra');
const path = require('path');
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
console.log("init runtime paths successfull")
module.exports = {
    antdPath,
    momentPath,
    reactPath,
    react_dom_path,
    systemjsPath,
    bizchartsPath
}
