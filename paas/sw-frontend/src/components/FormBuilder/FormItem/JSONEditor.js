/**
 * Created by caoshuaibiao on 2019/3/26.
 * json编辑器Item 支持json编辑前后的diff
 */

import React from 'react';
import { Spin, Button, Card, Modal, Tooltip, List, Row, Col, Divider, message } from 'antd';
import localeHelper from '../../../utils/localeHelper';
import JsonEditor from '../../JsonEditor';
import _ from 'lodash';

export default class JSONEditor extends React.Component {

    constructor(props) {
        super(props);
        let { model, onChange } = this.props;
        let initJson = {};
        if (model.initValue) {
            if (typeof model.initValue === 'string') {
                initJson = JSON.parse(model.initValue)
            } else if (typeof model.initValue === 'object' || Array.isArray(model.initValue)) {
                initJson = model.initValue;
            }
        } else if (model.defModel && typeof model.defModel === 'string') {
            initJson = JSON.parse(model.defModel)
        }
        this.initJson = initJson;
        this.newJson = initJson;
        onChange && onChange(initJson);
    }

    editorChange = (json) => {
        this.newJson = json;
        let { onChange } = this.props;
        onChange && onChange(json);
    };

    jsonEditorInitialized = (jsonEditor) => {
        this.jsonEditor = jsonEditor;
    };

    showDiff = () => {
        let oldJson = _.isString(this.initJson) ? JSON.parse(this.initJson) : this.initJson;
        let newJson = _.isString(this.newJson) ? JSON.parse(this.newJson) : this.newJson;
        Modal.info({
            content: (
                <div>
                    需要增加diff组件
                </div>
            ),
            width: '80%',
            onOk() { },
        });
    };

    render() {
        let options = {
            modes: ['code', 'tree']
        };
        let { model } = this.props;
        let { defModel = {} } = model;
        return (
            <div>
                <JsonEditor json={this.initJson} mode="code" readOnly={false} options={options} onChange={this.editorChange} changeInterval={500} initialized={this.jsonEditorInitialized} defaultExpand={true} style={{ height: model.height || '80vh' }} />
                {model.showDiff !== false && !defModel.disableShowDiff && <a onClick={this.showDiff}>{localeHelper.get('formItem.versionComparison', '新旧版本对比')}</a>}
            </div>
        )
    }
}



