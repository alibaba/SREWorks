/**
 * Created by caoshuaibiao on 2020/12/9.
 */
import React from "react";
import { Layout, Divider, Dropdown, Menu, Switch, Avatar } from "antd";
import SiderNavToggleBar from '../../components/SiderNavToggleBar';
import { connect } from 'dva';
import localeHelper from "../../utils/localeHelper";
import { LogoutOutlined } from "@ant-design/icons";
import DropDownUser from "../common/DropDownUser";
import properties from "../../properties";
@connect(({ home, global }) => ({
  home: home,
  global: global
}))
export default class HomeHeader extends React.Component {
  turnToDevops = () => {
    let path = "#/swadmin"
    if (properties.envFlag && properties.envFlag === 'PaaS') {
      path = "#/flyadmin"
    }
    window.open(path, "_blank")
  }
  render() {
    const { currentUser } = this.props.global;
    const { platformLogo, platformName } = properties;
    const UserTitle = () => (
      <div style={{ display: "flex", alignItems: "center" }}>
        <Avatar
          style={{ verticalAlign: 'middle' }}
          src={`https://work.alibaba-inc.com/photo/${currentUser.empId}.30x30.jpg`}
        />
        <div style={{ display: 'flex', flexDirection: 'column', paddingLeft: '5px', color: 'rgba(255,255,255,0.8)' }}>
          <span style={{ maxWidth: 50, lineHeight: 1.6 }} className="text-overflow">{!currentUser ? '' : currentUser.nickNameCn || currentUser.nickName || currentUser.loginName || currentUser.name}</span>
        </div>
      </div>
    );
    return (
      <div className="header">
        <div className="left-logo">
          <div>
            <SiderNavToggleBar theme="dark" />
          </div>
          <div>
            <span className="logo-link" onClick={() => { window.open("/#", "_blank") }} style={{ position: "relative" }}><img style={{
              width: 14, margin: "0px 6px 2px 6px",
              textAlign: "center"
            }} src={platformLogo} /></span>
            <span>{platformName}</span>
            <span><Divider style={{borderColor:'white'}} type="vertical" /></span>
            <span>运维桌面</span>
            <span><Divider type="vertical" /></span>
            <span className="logo-link" onClick={this.turnToDevops}>运维中台</span>
          </div>
        </div>
        <div className="center">

        </div>
        <div className="right-user">
          <DropDownUser isOnlyLogout>
            {/* <span className="block-item"><img className="userImg" alt="userImg" src={`https://work.alibaba-inc.com/photo/${currentUser.empId}.30x30.jpg`} /></span> */}
          </DropDownUser>
        </div>
      </div>
    );
  }
}
