import React, { Component } from 'react'
import PropTypes from 'prop-types'
import _ from 'lodash'

let WrapLog = (NeedLogComponent, sreworksSearchPath) => {
  class InnerComponent extends Component {
    constructor(props) {
      super(props)
    }

    removeNoNeedProps(allProps, extendList) {
      const res = _.assign({}, allProps)
      _.forEach(extendList, (l) => delete res[l])
      return res
    }

    render() {
      const needLogComponentProps = this.removeNoNeedProps(this.props, ['logParams'])

      return (
        <span>
          <NeedLogComponent {...needLogComponentProps} />
        </span>
      )
    }
  }

  InnerComponent.propTypes = {
    logParams: PropTypes.object,
  }

  return class WrapComponent extends Component {
    render() {
      return <InnerComponent {...this.props} />
    }
  }
}

export default WrapLog
