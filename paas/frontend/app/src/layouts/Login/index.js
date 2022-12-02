import React, { Component } from 'react'
// import { connect } from 'react-redux'
import FormWrapper from './formWrapper'
import { message, Row, Col } from 'antd'
import { localeHelper } from '@sreworks/shared-utils'
import properties from '../../properties'
// let properties = window.PROPERTY

import './index.less'

class LoginContainer extends Component {
  loginOption
  params
  constructor(props) {
    super(props)
    this.state = {
      loading: false,
      inputLoading: false,
    }
  }
  render() {
    const { platformName, platformLogo } = properties
    return (
      <div className="login_page">
        <div className="datav-login">
          <Row className="datav-rectangle">
            <Col span="12" className="login-left">
              <div>
                <img src={platformLogo} className="logo" />
              </div>
            </Col>
            <Col span="12" className="login-right">
              <h2 className="login-title">{platformName}</h2>
              <FormWrapper
                loginOption={this.loginOption}
                onSubmit={this.onSubmit.bind(this)}
                submitLoading={this.submitLoading.bind(this)}
                loading={this.state.inputLoading}
                layout={{
                  labelCol: { span: 7, offset: 0 },
                  wrapperCol: { span: 23 },
                }}
              />
            </Col>
          </Row>
        </div>
      </div>
    )
  }
  onSubmit(value) {
    message.info(localeHelper.get('LoginFormSubmitSuccess', '登录成功'))
    window.location.reload()
  }

  submitLoading(value) {
    if (value) {
      this.setState({ inputLoading: true })
    } else {
      this.setState({ inputLoading: false })
    }
  }
}
export default LoginContainer
