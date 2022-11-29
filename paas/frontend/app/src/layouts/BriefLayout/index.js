/**
 * Created by caoshuaibiao on 2021/2/25.
 */
import React from 'react'
import PageContent from './PageContent'
import Header from './Header'

import './index.less'

export default class BriefLayout extends React.Component {
  constructor(props) {
    super(props)
  }

  render() {
    const { headerProps } = this.props
    return (
      <div className="brief-layout">
        <Header {...headerProps} />
        <div id="top_progress" style={{ height: 1, marginTop: -1, zIndex: 10 }} />
        <PageContent {...this.props} />
      </div>
    )
  }
}
