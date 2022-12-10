import React, { Component } from 'react'
import { JSXRender } from '@sreworks/components'
import './index.less'
import Property from '../../Property'
let properties = Property.getProperties()

export default class SwitchCard extends Component {
  render() {
    let { widgetConfig = {} } = this.props
    let { backgroundImg = '', jsxDom, height, imgPosition = 'left' } = widgetConfig
    return (
      <div className={`content-wrapper-${imgPosition}`}>
        <div
          style={{
            height: Number(height) || 511,
            width: '65%',
            background: `url(${properties.baseUrl + backgroundImg}) no-repeat`,
            backgroundSize: '100% 100%',
          }}
        ></div>
        <JSXRender style={{ height: Number(height) || 511, width: '35%' }} jsx={jsxDom} />
      </div>
    )
  }
}
