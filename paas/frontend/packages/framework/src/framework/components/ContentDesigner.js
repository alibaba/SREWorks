/**
 * Created by caoshuaibiao on 2020/12/2.
 * 页面内容设计器
 */
import React from 'react'
import ContentLayout from './ContentLayout'

export default class ContentDesigner extends React.Component {
  constructor(props) {
    super(props)
    this.state = {}
  }

  render() {
    return (
      // <Card size="small" type="inner" bordered={false} extra={<ContentToolbar {...this.props}/>} style={{ width: '100%' }} bodyStyle={{padding:0}}>
      <ContentLayout {...this.props} />
      // </Card>
    )
  }
}
