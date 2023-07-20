import dva from 'dva'
import createLoading from 'dva-loading'
import { createHashHistory } from 'history'
import router from './router'
import AppService from './services/appService'
//不能去掉用于引入less.js来换肤使用
import less from 'less'
// import 'antd/dist/antd.less'
import 'antd/dist/antd.css'
import 'react-grid-layout/css/styles.css'
import 'react-resizable/css/styles.css'
import { util } from '@sreworks/shared-utils'
import global from './models/global'
import node from './models/node'
import home from './models/home'
import './index.less'
import '@sreworks/framework/dist/theme/index.css'
import '@sreworks/widgets/dist/theme/index.css'
import '@sreworks/components/dist/theme/index.css'
import { Property as util_properties } from '@sreworks/shared-utils'
import { Property as components_properties } from '@sreworks/components'
import { Property as widgets_properties } from '@sreworks/widgets'
import { Property as frame_properties } from '@sreworks/framework'
import properties from './properties'

util_properties.injectProperties(properties)
components_properties.injectProperties(properties)
widgets_properties.injectProperties(properties)
frame_properties.injectProperties(properties)

const app = dva({
  history: createHashHistory(),
  onError(error) {
    console.error(error.message)
  },
})

app.use(createLoading())
app.model(global)
app.model(home)
app.model(node)
// const modelsContext = require.context('./', true, /^\.\/models+\/[\s\S]*\.js$/)
// const models = modelsContext.keys().map((key) => modelsContext(key), [])

// models.forEach((m) => app.model(m))

//读取样式,根据不同的应用场景需要动态的适配
let themeType = localStorage.getItem('sreworks-theme')
if (!themeType) {
  localStorage.setItem('sreworks-theme', 'light')
}
if (themeType === 'dark') {
  themeType = 'navyblue'
  localStorage.setItem('sreworks-theme', 'navyblue')
}
{
  /* global THEMES */
}
if (themeType === 'navyblue') window.less.modifyVars(THEMES[themeType])
app.router(router)
// 入口前初始化桌面数据 util.getNewBizApp().split(",")[1]
;(async function () {
  let params = {
    namespaceId: (util.getNewBizApp() && util.getNewBizApp().split(',')[1]) || '',
    stageId: (util.getNewBizApp() && util.getNewBizApp().split(',')[2]) || '',
    visit: true,
  }
  try {
    const isLogined = await AppService.isLogined()
    if (isLogined && isLogined['name']) {
      const res = await AppService.getAllProfile(params)
      let { collectList = [], customQuickList = [], workspaces } = res.profile
      app.start('#root')
      app._store.dispatch({
        type: 'home/setDesktopData',
        workspaces,
        collectList,
        customQuickList,
        isEqual: res.isEqual,
      })
    } else {
      return false
    }
  } catch (error) {
    app.start('#root')
  }
})()
