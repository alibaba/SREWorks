import React, { Component } from 'react'
import AceViewer from '../FormBuilder/FormItem/AceViewer'
import 'brace/mode/json'

export default class JsonEditor extends Component {
  render() {
    let { style = { height: '220px' }, json, onChange, readOnly } = this.props
    return (
      <AceViewer
        model={{ defModel: { disableShowDiff: true, mode: 'json', ...style } }}
        readOnly={readOnly}
        {...style}
        mode="json"
        value={Object.assign({}, json)}
        onChange={onChange}
      />
    )
  }
}
