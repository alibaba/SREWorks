/**
 * Created by caoshuaibiao on 2020/12/9.
 */
import React from 'react'
import { Divider } from 'antd'
import SiderNavToggleBar from '../../components/SiderNavToggleBar'
import { connect } from 'dva'
import DropDownUser from './DropDownUser'

let properties = window.PROPERTY
@connect(({ home, global }) => ({
  home: home,
  global: global,
}))
export default class HomeHeader extends React.Component {
  turnToDevops = () => {
    let path = '#/swadmin'
    if (properties.envFlag && properties.envFlag === 'PaaS') {
      path = '#/flyadmin'
    }
    window.open(path, '_blank')
  }
  render() {
    const { currentUser } = this.props.global
    const { platformLogo, platformName } = properties
    return (
      <div className="header">
        <div className="left-logo">
          <div>
            <SiderNavToggleBar theme="dark" />
          </div>
          <div>
            <span
              className="logo-link"
              onClick={() => {
                window.open('/#', '_blank')
              }}
              style={{ position: 'relative' }}
            >
              <img
                style={{
                  width: 14,
                  margin: '0px 6px 2px 6px',
                  textAlign: 'center',
                }}
                src={platformLogo}
              />
            </span>
            <span>{platformName}</span>
            <span>
              <Divider style={{ borderColor: 'white' }} type="vertical" />
            </span>
            <span>运维桌面</span>
            <span>
              <Divider type="vertical" />
            </span>
            <span className="logo-link" onClick={this.turnToDevops}>
              运维中台
            </span>
          </div>
        </div>
        <div className="center"></div>
        <div className="right-user">
          <DropDownUser isOnlyLogout></DropDownUser>
        </div>
      </div>
    )
  }
}
