/**
 * Created by wangkaihua on 2021/4/26.
 * 警告提示
 */
import React from 'react'
import { Alert as AntdAlert } from 'antd'
import PropTypes from 'prop-types'

import './index.less'

export function Alert(props) {
  let { widgetConfig = {} } = props
  let { message, alertType, showIcon, closable, icon, description } = widgetConfig
  return (
    <AntdAlert
      message={message}
      type={alertType}
      showIcon={showIcon}
      closable={closable}
      icon={icon}
      description={description}
    />
  )
}

Alert.propTypes = {
  widgetConfig: PropTypes.object,
}
