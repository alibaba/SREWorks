import React, { Component } from 'react'
import _ from 'lodash'
import { JSXRender } from '@sreworks/components'

export default class CustomRender extends Component {
  render() {
    const jsx = _.get(this.props, 'widgetConfig.jsxDom', '<span>请填写组件的jsx属性</span>')
    return <JSXRender {...this.props} jsx={jsx} />
  }
}
