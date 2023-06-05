class Property {
  constructor(props) {
    this.properties = {
      ENV: {
        Standalone: 'Standalone', //软件化输出
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
let properties = Property.getInstance()
export default properties
