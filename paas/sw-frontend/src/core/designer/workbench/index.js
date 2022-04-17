/**
 * Created by caoshuaibiao on 2020/11/3.
 * 前端开发工作台
 */
import React from 'react';
import { Icon as LegacyIcon } from '@ant-design/compatible';
import { Layout, Menu, Breadcrumb, Card, Spin, message, Switch } from 'antd';
import MenuNavigation from "./MenuNavigation";
import NodeNavigation from "./NodeNavigation";
import NodeModel from '../../framework/model/NodeModel';
import  FormBlockEditor from '../editors/FormBlockEditor';
import  BlockEditor from '../editors/BlockEditor';
import  PageEditor from '../editors/PageEditor';
import { page_template_meta,template_app_id } from '../editors/TemplateConstant';
import appMenuTreeService from '../../services/appMenuTreeService';
import _ from 'lodash';
import uuidv4 from 'uuid/v4';

import './index.less';
import service from '../../framework/legacy/widgets/flyadmin/service';
const noDataImg =require('appAssets/img/no-data.png');
const { Content, Sider } = Layout;

export default class Workbench extends React.Component {

    static defaultProps = {
        nodeGroupData:[]
    };

    constructor(props) {
        super(props);
        this.state={
            menuFold:false,
            tabFold:false,
            currentEditorModel:false,
            contentLoading:false,
            nodeTypeId:null,
            nodeGroupData:props.nodeGroupData
        }
    }

    componentDidMount() {
        //this.loadNodeModel("xxx");
    }
    loadNodeModel=(nodeTypeId,nodeData)=>{
        if(nodeTypeId===this.state.nodeTypeId){
            return ;
        }
        this.setState({
            nodeTypeId:null,
            nodeData:nodeData,
            contentLoading:true
        });
        console.log(nodeTypeId,'nodeTypeId');
        if(nodeTypeId){
            let nodeModel=new NodeModel({nodeId:nodeTypeId});
            nodeModel.load('dev').then(result=>{
                this.setState({
                    nodeModel:nodeModel,
                    currentEditorData:nodeModel.getPageModel(),
                    editorType:"MAIN_PAGE",
                    editorKey:"MAIN_PAGE",
                    nodeTypeId:nodeTypeId,
                    contentLoading:false,
                    nodeGroupData:nodeModel.getGroupData()
                });
            })
        }
    };

    onMainPageClick=()=>{
        let {nodeModel}=this.state;
        this.setState({
            currentEditorData:nodeModel.getPageModel(),
            editorKey:"MAIN_PAGE",
            editorType:"MAIN_PAGE"
        });
    };

    onNodeClick = (nodeTypeId,nodeData) => {
        this.loadNodeModel(nodeTypeId,nodeData);
    };

    handleSaveItem=(item)=>{
        let {nodeModel}=this.state;
        this.setState({
            contentLoading:true,
        });
        nodeModel.saveItem(item).then(result=>{
            this.setState({
                contentLoading:false,
                nodeGroupData:nodeModel.getGroupData()
            });
        });
    };

    handleAddItem=(item)=>{
        let {nodeModel}=this.state;
        nodeModel.addItem(item);
        this.setState({
            nodeGroupData:nodeModel.getGroupData()
        });
    };

    handleDeleteItem=(item)=>{
        let {nodeModel}=this.state;
        this.setState({
            contentLoading:true,
        });
        nodeModel.removeItem(item).then(result=>{
            this.setState({
                contentLoading:false,
                nodeGroupData:nodeModel.getGroupData(),
                currentEditorData:nodeModel.getPageModel(),
                editorKey:"MAIN_PAGE",
                editorType:"MAIN_PAGE"
            });
        });

    };

    handleItemClick=(item)=>{
        this.setState({
            currentEditorData:item,
            editorKey:item.id,
            editorType:item.type
        });
    };

    handleSaveMainPage=()=>{
        let {nodeModel}=this.state;
        this.setState({
            contentLoading:true
        });
        nodeModel.savePageModel().then(result=>{
            this.setState({
                contentLoading:false
            });
        }).catch(err=>{
            message.error(JSON.stringify(err));
            this.setState({
                contentLoading:false
            });
        });
    };
    saveAsTemplate=(templateServiceType)=> {
        let {nodeModel}=this.state;
        let unKey = uuidv4();
        let newParam = nodeModel.getPageModel().toJSON();
        let cloneParams = _.cloneDeep(newParam);
        let excReg = new RegExp("/"+this.props.appId+"/g");
        console.log(cloneParams,'cloneParams-cloneParams')
        cloneParams.nodeTypePath = page_template_meta.parentNodeTypePath + `::${templateServiceType}`;
        cloneParams.name = unKey;
        cloneParams.label = unKey;
        cloneParams.id = unKey;
        // cloneParams = JSON.parse(JSON.stringify(cloneParams).replace(excReg,template_app_id))
        this.setState({
            contentLoading:true
        });
        appMenuTreeService.saveMainPage(cloneParams).then(res => {
            this.saveAsBlocks(templateServiceType);
            this.setState({
                contentLoading:false
            });
        }).finally(error=> {
            this.setState({
                contentLoading:false
            });
        });

    }
    saveAsBlocks=(templateServiceType)=> {
        let { nodeGroupData } = this.state;
        let cloneGroupData = _.cloneDeep(nodeGroupData),paramsArray = [];
        console.log(cloneGroupData,cloneGroupData[0],cloneGroupData[0].items,'cloneGroupData-cloneGroupData')
        cloneGroupData[0] && cloneGroupData[0].items.forEach(item => {
            item.nodeTypePath = page_template_meta.parentNodeTypePath + `::${templateServiceType}`
            item.appId = template_app_id;
            let newBlockId = uuidv4();
            let {appId,nodeTypePath,type,version,config,name} = item;
            config.id = newBlockId;
            config.name = newBlockId;
            let obj = {
                appId,nodeTypePath,type,version,config,name:newBlockId
            }
            paramsArray.push(obj);
        })
        console.log(paramsArray,'cloneGroupData-cloneGroupData-2')
        paramsArray.forEach(obj=> {
            let {nodeTypePath} = obj;
            appMenuTreeService.saveElement(obj).then(res => {
                let {elementId} = res;
                appMenuTreeService.attachNode(nodeTypePath,elementId);
            });
        })
    }
    handleContentResize=()=>{
        /*setTimeout(function () {
            let event = new Event('resize');
            window.dispatchEvent(event);
        },500)*/
    };
    render() {
        let {menuFold,tabFold,currentEditorData,editorType,nodeGroupData,nodeTypeId,editorKey,contentLoading,nodeModel,nodeData}=this.state,{stageHeight=660,style,...otherProps}=this.props;
        let contentHeight=stageHeight-40;
        console.log(nodeGroupData,'nodeGroupData-nodeGroupData')
        return (
                <Layout className="abm_frontend_designer" style={{height:stageHeight}}>
                <Sider trigger={null} collapsible collapsed={menuFold} width={170} collapsedWidth={32}>
                    {menuFold && <div className="fold-name">菜单节点树</div>}
                    <div style={{display: menuFold ? "none" : ""}}>
                        <div style={{height:36}} className="menu-tree-header">
                            <div style={{marginTop:-5,display:'flex',width:'100%',justifyContent:'space-between'}}>
                                {!menuFold && <div style={{width: "100%", textAlign: "center", opacity: 0.65}}>
                                    <h4>菜单节点树</h4>
                                </div>}
                            </div>
                        </div>
                        <div style={{overflowY:"auto",height:contentHeight}}>
                            <MenuNavigation appId={this.props.appId || "front-dev"} menuFold={menuFold} onNodeClick={this.onNodeClick} />
                        </div>
                    </div>
                </Sider>
                <div onClick={(e)=>{this.setState({menuFold:!menuFold},()=>this.handleContentResize())}} className="collapsed-btn-front-menu globalBackground">
                    <LegacyIcon type={menuFold ? 'right' : 'left'}/>
                </div>
                <Content className={!nodeTypeId?"globalBackground":""} style={{height: stageHeight}}>
                    <Spin spinning={contentLoading}>
                        {
                            nodeTypeId &&
                            <Layout>
                                <Sider style={{marginLeft: 2}} trigger={null} collapsible collapsed={tabFold}
                                       collapsedWidth={32} width={181}>
                                    {tabFold && <div className="fold-name">页面设计器</div>}
                                    <div style={{display: tabFold ? "none" : ""}}>
                                        <div style={{height: 36}} className="menu-tree-header">
                                            <div style={{
                                                marginTop: -5,
                                                display: "flex",
                                                width: "100%",
                                                justifyContent: "space-between",
                                            }}>
                                                <div style={{width: "100%", textAlign: "center", opacity: 0.65}}>
                                                    <h4>页面设计器</h4>
                                                </div>
                                            </div>
                                        </div>
                                        <div className="node-navigation" style={{overflowY:"auto",height:contentHeight,overflowX:"hidden"}}>
                                            <NodeNavigation mainPageClick={this.onMainPageClick}
                                                            onAddItem={this.handleAddItem}
                                                            onItemClick={this.handleItemClick}
                                                            nodeGroupData={nodeGroupData}
                                            />
                                        </div>
                                    </div>
                                </Sider>
                                <div onClick={(e) => {
                                    this.setState({tabFold: !tabFold},()=>this.handleContentResize());
                                }} className="collapsed-btn-front-menu globalBackground">
                                    <LegacyIcon type={tabFold ? "right" : "left"}/>
                                </div>
                                <Content className="globalBackground"
                                         style={{height: stageHeight, zIndex: 100, marginLeft: 2}}>
                                    {editorType === "MAIN_PAGE" &&
                                    <PageEditor {...otherProps}
                                                contentLoading={contentLoading}
                                                height={stageHeight}
                                                nodeData={nodeData}
                                                routeData={nodeData}
                                                pageModel={nodeModel.getPageModel()} key={editorKey}
                                                nodeModel={nodeModel}
                                                saveAs={this.saveAsTemplate}
                                                onSave={this.handleSaveMainPage}/>}
                                    {editorType === "FORM" &&
                                    <FormBlockEditor {...otherProps} editorData={currentEditorData}
                                                     nodeModel={nodeModel}
                                                     onSave={this.handleSaveItem} key={editorKey}
                                                     onDelete={this.handleDeleteItem}
                                    />
                                    }
                                    {editorType === "BLOCK" &&
                                    <BlockEditor {...otherProps}
                                                 nodeData={nodeData}
                                                 height={stageHeight}
                                                 editorData={currentEditorData}
                                                 onDelete={this.handleDeleteItem}
                                                 onSave={this.handleSaveItem}
                                                 nodeModel={nodeModel}
                                                 key={editorKey}/>}
                                </Content>
                            </Layout>
                        }
                        {
                            !nodeTypeId && <div className="no-data globalBackground">
                                <div className="img-wrapper">
                                    <img width={200} src={noDataImg}/>
                                </div>
                                <div className="no-data-text">
                                    暂无数据，请点击左边菜单节点树进行配置哦
                                </div>
                            </div>
                        }
                    </Spin>
                </Content>
            </Layout>

        );
    }
}