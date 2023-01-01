class Property {
  constructor(props) {
    this.properties = {
      ENV: {
        //运行环境定义
        ApsaraStack: 'ApsaraStack', //标专
        DXZ: 'DXZ', //大小专
        OXS: 'OXS', //OXS
        Internal: 'Internal', //对内()
        Standalone: 'Standalone', //软件化输出
        RQY: 'RQY', //敏捷版
        PaaS: 'PaaS', //paas化版本
      },
      name: 'SREWorks',
      // envFlag: "PaaS",
      envFlag: 'Standalone',
      defaultProduct: 'desktop',
      defaultNamespace: 'default',
      defaultStageId: 'dev',
      deployEnv: process.env.DEPLOY_ENV || 'local',
      platformName: 'SREWorks',
      platformLogo: '/static/icons/new_favicon.png',
    }
  }

  injectProperties(properties) {
    console.log('extra properties', properties)
    Object.assign(this.properties, properties)
  }

  getProperties() {
    return this.properties
  }
}

Property.getInstance = (function () {
  let instance
  return function () {
    if (!instance) {
      instance = new Property()
    }
    return instance
  }
})()
export default Property.getInstance()
