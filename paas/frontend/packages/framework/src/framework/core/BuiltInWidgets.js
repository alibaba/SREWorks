/**
 * Created by caoshuaibiao on 2020/12/17.
 * 内置组件集合
 */
import service from '../../services/appMenuTreeService'
import { SRE_builtInWidgets, SRE_builtInWidgetMetaMapping } from '@sreworks/widgets'
let builtInWidgets = {}
let builtInWidgetMetaMapping = {}
//扫描内置挂件包,生成内置挂件映射 todo----暂时未将@sreworks/widgets 纳入
// const widgetsContext = require.context('./', true, /^\.\/widgets\/((?!\/)[\s\S])+\/index\.js$/)
const widgetsContext = require.context('./widgets/', true, /index\.js$/)
widgetsContext.keys().forEach((key) => {
  //获取每个挂件包,以包名为key注册到内置组件映射挂件对象上
  // let widgetName = key.split('/')[2]
  let reg = new RegExp('index.js', 'g')
  let widgetName = key.split('/')[1].replace(reg, '')
  builtInWidgets[widgetName] = widgetsContext(key)
})
//扫描内置挂件定义元数据,生成内置挂件列表 todo----暂时未将@sreworks/widgets 纳入
// const widgetMetasContext = require.context('./', true, /^\.\/widgets\/((?!\/)[\s\S])+\/meta\.js$/)
const widgetMetasContext = require.context('./widgets/', true, /meta\.js$/)
widgetMetasContext.keys().forEach((key) => {
  //获取每个挂件包下的meta定义,如果未定义type以包名为type
  // let widgetName = key.split('/')[2]
  let reg = new RegExp('meta.js', 'g')
  let widgetName = key.split('/')[1].replace(reg, '')
  const meta = widgetMetasContext(key)
  if (!meta.type) {
    meta.type = widgetName
  }
  //TODO 后续对内置组件展示有顺序需求可在此进行排序
  builtInWidgetMetaMapping[meta.type] = meta
})
builtInWidgetMetaMapping = Object.assign({}, builtInWidgetMetaMapping, SRE_builtInWidgetMetaMapping)
builtInWidgets = Object.assign({}, builtInWidgets, SRE_builtInWidgets)

function getCustomCompList() {
  return service.getCustomList()
}
export function getBuiltInWidget(model) {
  return builtInWidgets[model.type]
}
export function getBuiltInWidgetMetaMapping() {
  // let res = await getCustomCompList()
  // let customTemplate = widgetCategory.find(item => item.name === 'custom') ? widgetCategory.find(item => item.name === 'custom')['children'][0] : {};
  // res && res.forEach(item => {
  //     let cloneTemplate = cloneDeep(customTemplate);
  //     cloneTemplate['id'] = item.componentId;
  //     cloneTemplate['title'] = item.name;
  //     cloneTemplate['name'] = item.name;
  //     cloneTemplate['info']['description'] = (item.configObject && item.configObject.description) || '组件使用说明';
  //     if (item['configObject'] && item['configObject']['icon']) {
  //         cloneTemplate['info']['logos']['small'] = item['configObject']['icon']
  //     }
  // })
  return builtInWidgetMetaMapping
}

export function getBuiltInWidgets() {
  return builtInWidgets
}
