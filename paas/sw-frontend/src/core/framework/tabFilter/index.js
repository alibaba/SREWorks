import React, { Component } from 'react';
import { Tabs, Radio } from "antd";
import './index.less';
const { TabPane } = Tabs;

class TabFilter extends Component {
    constructor(props) {
        super(props);
        let { scenes = [], items } = props, defaultFilter;
        items.forEach(ni => {
            let { defModel = {} } = ni;
            if (defModel.defaultFilter) {
                defaultFilter = ni;
            }
        });
        if (!defaultFilter) defaultFilter = items[0];
        this.state = {
            activeKey: items[0]['name'] || '',
            scenes: scenes,
            defaultFilter: defaultFilter
        }
    }
    handleSceneChanged = (activeKey) => {
        let { onSubmit } = this.props;
        if (activeKey.target) {
            activeKey = activeKey.target.value
        }
        onSubmit(activeKey);
        this.setState({
            activeKey
        })
    };
    render() {
        let { scenes = [], defaultFilter, activeKey } = this.state;
        let { items = [], action, widgetData } = this.props;
        if (widgetData && Object.values(widgetData).length) {
            items = Object.values(widgetData)
        }
        const { TabPane } = Tabs;
        let { tabType, tabSize } = action;
        if (tabSize === 'default' && tabType === 'Button') {
            tabSize = 'middle'
        }
        if (tabType === 'Button') {
            return (
                <Radio.Group size={tabSize} onChange={this.handleSceneChanged} defaultValue={items[0].name}>
                    {
                        items && items.map(pan => (<Radio.Button value={pan.name}>{pan.label}</Radio.Button>))
                    }
                </Radio.Group>
            )
        }
        if (tabType === 'Switch') {
            return (
                <section className={`${tabSize}-model`}>
                    {
                        items && items.map(pan => (<div className={`switch-cell ${pan.name === activeKey ? 'active-model' : ''}`} onClick={() => this.handleSceneChanged(pan.name)} key={pan.name}>{pan.label}</div>))
                    }
                </section>
            )
        }
        return (
            <Tabs style={{ marginBottom: '-17px' }} size={tabSize} defaultActiveKey={items[0].name} onChange={this.handleSceneChanged}>
                {
                    items && items.map(pan => (<TabPane tab={pan.icon ? (<span>{pan.icon}{pan.label}</span>) : pan.label} key={pan.name}></TabPane>))
                }
            </Tabs>
        );
    }
}

export default TabFilter;