/**
 * @author caoshuaibiao
 * @date 2021/6/29 20:20
 * @Description:流式页面布局定义器
 */
import React, { Component } from "react";
import { ReactSortable } from "react-sortablejs";
import { Button, Tooltip, Modal } from 'antd';
import { PlusOutlined, CloseOutlined, CopyOutlined, DeleteOutlined, EditOutlined } from '@ant-design/icons';
import RowContainerHandler from "./RowContainerHandler";
import RowSetting from "./RowSetting";
import uuidv4 from 'uuid/v4';


import './index.less';
import WidgetModel from "../model/WidgetModel";
import Constants from "../model/Constants";

class FluidContentLayoutDesigner extends Component {

    constructor(props) {
        super(props);
        //流式布局定义为一个组件存在
        console.log(props, props.containerModel, 'props.containerModel');
        let { widgets, nodeModel } = props.containerModel;
        let fluidWidget = widgets[0];
        if (!fluidWidget) {
            fluidWidget = new WidgetModel({
                type: "FluidGrid",
                name: "FluidGrid",
                id: "FluidGrid",
                config: {
                    rows: []
                }
            })
         props.containerModel.widgets.push(fluidWidget);
        }
        //bad small
        fluidWidget.config && fluidWidget.config.rows && fluidWidget.config.rows.map(row => {
            row.elements.forEach((element, index) => {
                let widgetModel = new WidgetModel(element);
                widgetModel.setNodeModel(nodeModel);
                row.elements[index] = widgetModel;
            })
        });
        this.fluidWidget = fluidWidget;
        let rows = (fluidWidget.config && fluidWidget.config.rows) || [];
        this.state = {
            rows: rows
        };
    }

    handleRowsChanged = (rows) => {
        this.setState({
            rows: rows
        });
        this.fluidWidget.config.rows = rows;
    }


    handleAddRow = () => {
        let defaultRow = {
            spans: "12,12",
            uniqueKey: uuidv4(),
            elements: [],
        };
        let { rows } = this.state;
        Modal.confirm({
            title: ' 添加行布局',
            icon: '',
            width: 640,
            content: <div><RowSetting row={defaultRow} onValuesChange={(changedValues, allValues) => Object.assign(defaultRow, allValues)} /></div>,
            onOk: () => {
                let newRows = [...rows, defaultRow];
                this.setState({
                    rows: newRows
                });
                this.handleRowsChanged(newRows);
            },
            okText: '添加',
            cancelText: '取消',
        });
    }

    handleRemoveRow = (row) => {
        let newRows = [], { rows } = this.state;
        for (let w = 0; w < rows.length; w++) {
            let trow = rows[w];
            if (trow.uniqueKey !== row.uniqueKey) {
                newRows.push(trow)
            }
        }
        this.setState({
            rows: newRows
        });
        this.handleRowsChanged(newRows);
    }

    handleRowUpdate = (upRow) => {
        let newRows = [], { rows } = this.state;
        for (let w = 0; w < rows.length; w++) {
            let trow = rows[w];
            if (trow.uniqueKey === upRow.uniqueKey) {
                newRows.push(upRow);
            } else {
                newRows.push(trow);
            }
        }
        this.handleRowsChanged(newRows);
    }

    handleRowsSort = (sortRows) => {
        this.setState({ rows: sortRows });
        this.handleRowsChanged(sortRows);
    };

    render() {
        let { rows } = this.state;
        return (
            <div className="fluid_page_designer_container">
                <ReactSortable
                    list={rows}
                    animation={200}
                    setList={(sortRows) => this.handleRowsSort(sortRows)}
                >
                    {rows.map((row) => (
                        <RowContainerHandler {...this.props} pageLayoutType={Constants.PAGE_LAYOUT_TYPE_FLUID} key={row.uniqueKey} row={row} onRemove={() => this.handleRemoveRow(row)} onUpdate={(upRow) => this.handleRowUpdate(upRow)} mode={Constants.WIDGET_MODE_EDIT} />
                    ))}
                </ReactSortable>
                <Button className="add_handler_button" type="solid" icon={<PlusOutlined />} onClick={this.handleAddRow}>添加行容器</Button>
            </div>
        );
    }
}

export default FluidContentLayoutDesigner