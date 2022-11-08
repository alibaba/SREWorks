/**
 * Created by caoshuaibiao on 2018/8/22.
 * 用户服务接口
 */
import { cacheRepository, httpClient } from '@sreworks/shared-utils'
import properties from '../properties'
//  let properties = window.PROPERTY;
const baseUrl = properties.baseUrl

const apiEndpoint = properties.apiEndpoint

const applicationApiPerfix = 'gateway/v2/foundation/application/'
const authPrefix = 'gateway/v2/common/authProxy/'
const opsAppPerfix = 'gateway/v2/common/productops/apps'

let loadTime = 0,
  loginStartPoint = 0
function getProductName() {
  //从路径获取应用名,如果没有则默认为tesla主站
  let productName = window.location.hash.split('/')[1]
  if (!productName) {
    if (properties.defaultProduct && !properties.defaultProduct.includes('$')) {
      productName = properties.defaultProduct
    } else {
      productName = 'desktop'
    }
  }
  if (productName.includes('?')) {
    productName = productName.split('?')[0]
  }
  return productName
}

class AppService {
  constructor() {}

  getAllProfile(params) {
    // console.log(props.home);
    //http://frontend.ca221ae8860d9421688e59c8ab45c8b21.cn-hangzhou.alicontainer.com/gateway/v2/foundation/appmanager/instances?namespaceId=default&stageId=dev&visit=true&pagination=false
    const { stageId, visit } = params
    return httpClient
      .get('gateway/v2/foundation/appmanager/realtime/app-instances', {
        params: {
          stageId,
          visit,
          page: 1,
          pageSize: 1000,
          optionKey: 'source',
          optionValue: 'swadmin',
        },
      })
      .then((res) => {
        //   服务端数据结构改变
        let transItems = []
        transItems =
          res.items &&
          res.items.length &&
          res.items.map((item) => {
            return Object.assign(item, {})
          })
        transItems &&
          transItems.forEach((item) => {
            if (item.options) {
              item.appName = item.options.name || ''
              item.logoUrl = item.options.logoImg || ''
              item.introduction = item.options.description || ''
              item.category = item.options.category
            }
            item.id = item.appInstanceId || ''
          })
        return httpClient
          .get('gateway/v2/foundation/appmanager/profile', { params: params })
          .then((profile) => {
            let copyProFile = _.cloneDeep(profile)
            let includesIndex = []
            let collectList = profile.collectList || []
            let workspaces = profile.workspaces || [
              {
                name: '新建桌面',
                type: 'custom',
                hasSearch: true,
                items: [],
                background: '../../assets/deskstop/deskstop_four.jpg',
              },
            ]
            let customQuickList = profile.customQuickList || []
            JSON.parse(JSON.stringify([...(transItems || []), ...customQuickList])).map((item) => {
              includesIndex.push(item.id)
            })
            workspaces.map((workspace) => {
              workspace.items = workspace.items.filter((item) => {
                return includesIndex.includes(item.id)
              })
            })
            // if (workspaces[0].items.length === 0) {
            //     alert(JSON.stringify(workspaces[0].items));
            // }
            //过滤掉没有权限的应用
            profile.collectList = collectList.filter((item) => {
              return includesIndex.includes(item.id)
            })
            profile.workspaces = workspaces
            profile.customQuickList = customQuickList
            return { appStore: transItems, profile, isEqual: _.isEqual(copyProFile, profile) }
          })
      })
  }
}

// 实例化后再导出
export default new AppService()

/**
 *定义些应用级别的设置对象
 */

export class ApplicationSetting {
  constructor(settingsData) {
    this.products = settingsData.products
    Object.assign(this, settingsData)
  }
}
