import React, { Component } from "react";
import { EditOutlined, MinusCircleOutlined, PlusOutlined } from '@ant-design/icons';
import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import { Tabs, Select, Button, Row, Col, Collapse, Input } from "antd";
// import FormElementFactory from '../../../components/FormBuilder/FormElementFactory';
import AceEditor from "react-ace";
import _ from "lodash";
import uuid from "uuid/v4";
import brace from 'brace';
import "brace/mode/json";
import 'brace/mode/javascript';
import 'brace/theme/monokai';
import 'brace/theme/xcode';

const Panel = Collapse.Panel;
const { TabPane } = Tabs;
const { Option } = Select;
const formItemLayout = {
    labelCol: {
        xs: { span: 24 },
        sm: { span: 4 },
    },
    wrapperCol: {
        xs: { span: 24 },
        sm: { span: 16 },
        md: { span: 16 },
    },
};
let themeType = localStorage.getItem('tesla-theme');
export default class SearchParamsEditor extends Component {
    constructor(props) {
        super(props);
        // let initValue = [{
        //     "id": "xxxxxxx",
        //     "type": "api",
        //     "names": ["appId", "appName"],
        //     "dependencies": [],
        //     "config": {
        //         "url": "http://prod-app-app.sreworks.svc.cluster.local:80/appdev/app/listAll",
        //         "method": "GET",
        //         "contentType": "",
        //         "headers": {},
        //         "params": {},
        //         "body": "",
        //         "fileds": [{
        //             "field": "$.data[*].appId",
        //             "type": "String",
        //             "alias": "appId"
        //         },
        //         {
        //             "field": "$.data[*].appName",
        //             "type": "Auto",
        //             "alias": "appName"
        //         }
        //         ]
        //     }
        // }]
        // if(props.value && props.value instanceof Array && props.value.length !==0){
        //     initValue = props.value
        // }
        // this.state = {
        //     visibleMap: {},
        //     value: initValue
        // };
    }
    editorChange=(finalValue)=> {
        console.log(finalValue,'执行了-2')
        let formateValue = [];
        if(finalValue){
            try {
                formateValue = finalValue
                this.props.onValuesChange && this.props.onValuesChange(formateValue)
            } catch (error) {
            }
        }
    }
    render() {
        // let {value} = this.state;
        // let initValue = [{
        //     "id": "xxxxxxx",
        //     "type": "api",
        //     "names": ["appId", "appName"],
        //     "dependencies": [],
        //     "config": {
        //         "url": "http://prod-app-app.sreworks.svc.cluster.local:80/appdev/app/listAll",
        //         "method": "GET",
        //         "contentType": "",
        //         "headers": {},
        //         "params": {},
        //         "body": "",
        //         "fileds": [{
        //             "field": "$.data[*].appId",
        //             "type": "String",
        //             "alias": "appId"
        //         },
        //         {
        //             "field": "$.data[*].appName",
        //             "type": "Auto",
        //             "alias": "appName"
        //         }
        //         ]
        //     }
        // }]
        let value = "[\n  {\n    \"id\": \"xxxxxxx\",\n    \"type\": \"api\",\n    \"names\": [\n      \"appId\",\n      \"appName\"\n    ],\n    \"dependencies\": [],\n    \"config\": {\n      \"url\": \"http://prod-app-app.sreworks.svc.cluster.local:80/appdev/app/listAll\",\n      \"method\": \"GET\",\n      \"contentType\": \"\",\n      \"headers\": {},\n      \"params\": {},\n      \"body\": \"\",\n      \"fileds\": [\n        {\n          \"field\": \"$.data[*].appId\",\n          \"type\": \"String\",\n          \"alias\": \"appId\"\n        },\n        {\n          \"field\": \"$.data[*].appName\",\n          \"type\": \"Auto\",\n          \"alias\": \"appName\"\n        }\n      ]\n    }\n  }\n]"
        // let {value} = this.props;
        console.log(this.props.value,'传入props.value')
        if(this.props.value && this.props.value.length >20){
            value = this.props.value
        }
        // let value = JSON.stringify(initValue,null,2);
        return <div className="card-tab-panel">
            <Form>
                <Form.Item name="searchParams"  {...formItemLayout} label={'页面定义参数'}>
                    <AceEditor
                        mode="json"
                        style={{ minHeight: 400, width: 700 }}
                        fontSize={12}
                        theme={themeType === 'light' ? "xcode" : "monokai"}
                        showPrintMargin={true}
                        showGutter={true}
                        defaultValue={value}
                        onChange={this.editorChange}
                        highlightActiveLine={true}
                        setOptions={{
                            enableBasicAutocompletion: false,
                            enableLiveAutocompletion: false,
                            enableSnippets: false,
                            showLineNumbers: true,
                            tabSize: 2,
                        }}
                    />,
                </Form.Item>
            </Form>
        </div>;
    }
}
// export default Form.create({
//     onValuesChange: (props, changedValues, allValues) => {
//         console.log("allValues:", allValues);
//         console.log("changedValues:", changedValues);
//         console.log("props:", props);
//         //存在隐藏项因此,需要进行值合并处理
//         props.onValuesChange && props.onValuesChange(changedValues, allValues.type ? Object.assign({}, props.value, allValues) : null);
//     },
// })(SearchParamsEditor);