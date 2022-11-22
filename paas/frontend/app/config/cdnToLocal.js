/*
 * @version: 2.0.0
 * @Author: deeham.ww
 * @Date: 2022-11-10 15:26:16
 * @LastEditors: deeham.ww
 * @LastEditTime: 2022-11-22 14:22:44
 */
const fs = require('fs-extra');
const path = require('path');
const paths = require('./paths');
// fs.copySync(path.join(ownPath, 'vue-template'), appPath);
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

const packagejson = getPackageJson();
const antdPath = getDependencyVesion(packagejson, 'antd');
const momentPath = getDependencyVesion(packagejson, 'moment');
const reactPath = getDependencyVesion(packagejson, 'react');
const react_dom_path = getDependencyVesion(packagejson, 'react-dom');
const systemjsPath = getDependencyVesion(packagejson, 'systemjs');
const bizchartsPath = getDependencyVesion(packagejson, 'bizcharts');
const vuePath = getDependencyVesion(packagejson, 'vue');
const elementUIPath = getDependencyVesion(packagejson, 'element-ui');
const vueraPath = getDependencyVesion(packagejson, 'vuera');
const reactAcePath = getDependencyVesion(packagejson, 'react-ace');
const lodashPath = getDependencyVesion(packagejson, 'lodash');
const html2canvasPath = getDependencyVesion(packagejson, 'html2canvas');
const jqueryPath = getDependencyVesion(packagejson, 'jquery');

const dependency_arr_init = (env='dev')=> {
    let workspacePath = env === 'dev' ? paths.appPublic : paths.appBuild
    try {
        return [{
            from: paths.appNodeModules + '/antd/dist/antd.min.js',
            to: workspacePath + '/common_vendor/antd/' + antdPath + '/antd.min.js'
        },
        {
            from: paths.appNodeModules + '/react/umd/react.production.min.js',
            to: workspacePath + '/common_vendor/react/' + reactPath + '/react.production.min.js'
        },
        {
            from: paths.appNodeModules + '/react-dom/umd/react-dom.production.min.js',
            to: workspacePath + '/common_vendor/react-dom/' + react_dom_path + '/react-dom.production.min.js'
        },
        {
            from: paths.appNodeModules + '/moment/min/moment.min.js',
            to: workspacePath + '/common_vendor/moment/' + momentPath + '/moment.min.js'
        },
        {
            from: paths.appNodeModules + '/systemjs/dist/system.min.js',
            to: workspacePath + '/common_vendor/systemjs/' + systemjsPath + '/system.min.js'
        },
        {
            from: paths.appNodeModules + '/vue/dist/vue.min.js',
            to: workspacePath + '/common_vendor/vue/' + vuePath + '/vue.min.js'
        },
        {
            from: paths.appNodeModules + '/vuera/dist/vuera.iife.js',
            to: workspacePath + '/common_vendor/vuera/' + vueraPath + '/vuera.iife.js'
        },
        {
            from: paths.appNodeModules + '/element-ui/lib/index.js',
            to: workspacePath + '/common_vendor/element-ui/' + elementUIPath + '/index.js'
        },
        {
            from: paths.appNodeModules + '/element-ui/lib/theme-chalk/index.css',
            to: workspacePath + '/common_vendor/element-ui/' + elementUIPath + '/index.css'
        },
        {
            from: paths.appNodeModules + '/bizcharts/umd/BizCharts.min.js',
            to: workspacePath + '/common_vendor/bizcharts/' + bizchartsPath + '/BizCharts.min.js'
        },
        {
            from: paths.appNodeModules + '/react-ace/dist/react-ace.min.js/main.js',
            to: workspacePath + '/common_vendor/react-ace/' + reactAcePath + '/main.js'
        },
        {
            from: paths.appNodeModules + '/lodash/lodash.min.js',
            to: workspacePath + '/common_vendor/lodash/' + lodashPath + '/lodash.min.js'
        },
        {
            from: paths.appNodeModules + '/jquery/dist/jquery.min.js',
            to: workspacePath + '/common_vendor/jquery/' + jqueryPath + '/jquery.min.js'
        },
        {
            from: paths.appNodeModules + '/html2canvas/dist/html2canvas.min.js',
            to: workspacePath + '/common_vendor/html2canvas/' + html2canvasPath + '/html2canvas.min.js'
        },
        ]
    } catch(error) {
        console.log('cdn library to local failed ',error)
    }
}
dependency_arr_init().forEach(item => {
    fs.copySync(item.from, item.to)
})
console.log("init runtime paths successfull")
let indexHtml = fs.readFileSync(paths.appPublic + '/index.html', 'utf8')
indexHtml = indexHtml.replace(/moment\/.([\s\S]){1,}\/moment.min.js/gm, `moment/${momentPath}/moment.min.js`).replace(/antd\/([\s\S]){1,}\/antd.min.js/gm, `antd/${antdPath}/antd.min.js`).replace(/react\/.([\s\S]){1,}\/react.production.min.js/gm, `react/${reactPath}/react.production.min.js`).replace(/react-dom\/.([\s\S]){1,}\/react-dom.production.min.js/gm, `react-dom/${react_dom_path}/react-dom.production.min.js`).replace(/systemjs\/([\s\S]){1,}\/system.min.js/gm, `systemjs/${systemjsPath}/system.min.js`)
fs.writeFileSync(paths.appPublic + '/index.html', indexHtml, 'utf8')
module.exports = {
    antdPath,
    momentPath,
    reactPath,
    react_dom_path,
    systemjsPath,
    bizchartsPath,
    vuePath,
    vueraPath,
    elementUIPath,
}
