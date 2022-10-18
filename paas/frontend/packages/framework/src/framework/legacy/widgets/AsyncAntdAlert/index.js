import React, { Component } from 'react'
import { Alert } from 'antd'
import _ from 'lodash'
import { util, httpClient } from '@sreworks/shared-utils'
import { JSXRender } from '@sreworks/components'

class AsyncAntdAlert extends Component {
  constructor(props) {
    super(props)
    this.state = {
      message: '',
      description: '',
    }
  }

  getParams() {
    return Object.assign(
      {},
      this.props.nodeParams,
      util.stringToObject(this.props.history.location.search),
    )
  }

  url(props) {
    return util.renderTemplateString(props.url || '', {
      ...props.defaultContext,
      ...props.nodeParams,
    })
  }

  UNSAFE_componentWillMount() {
    httpClient
      .get(util.renderTemplateString(_.get(this.props, 'mode.config.apiUrl', ''), this.getParams()))
      .then((data) => {
        this.setState({
          message: util.renderTemplateString(
            _.get(this.props, 'mode.config.message', ''),
            Object.assign({}, this.getParams(), data),
          ),
          description: util.renderTemplateString(
            _.get(this.props, 'mode.config.description', ''),
            Object.assign({}, this.getParams(), data),
          ),
        })
      })
  }

  componentDidUpdate(prevProps) {
    if (
      this.url(prevProps) !== this.url(this.props) ||
      !_.isEqual(prevProps.nodeParams, this.props.nodeParams)
    ) {
      httpClient
        .get(
          util.renderTemplateString(_.get(this.props, 'mode.config.apiUrl', ''), this.getParams()),
        )
        .then((data) => {
          this.setState({
            message: util.renderTemplateString(
              _.get(this.props, 'mode.config.message', ''),
              Object.assign({}, this.getParams(), data),
            ),
            description: util.renderTemplateString(
              _.get(this.props, 'mode.config.description', ''),
              Object.assign({}, this.getParams(), data),
            ),
          })
        })
    }
  }

  renderJSX = (jsx) => {
    return <JSXRender jsx={jsx} />
  }

  render() {
    let description = this.state.description
    if (description) {
      description = this.renderJSX(this.state.description)
    }
    return (
      <Alert
        message={this.renderJSX(this.state.message)}
        description={description}
        type={_.get(this.props, 'mode.config.type')}
        showIcon={_.get(this.props, 'mode.config.showIcon')}
      />
    )
  }
}

export default AsyncAntdAlert
