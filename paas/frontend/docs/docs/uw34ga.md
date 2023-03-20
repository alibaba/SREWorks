---
title: --- 备用文档 4.2.2 运维前端开发
date: 2022-03-25T03:39:22.000Z
toc_max_heading_level: 6
toc_min_heading_level: 2
---

<a name="Zelez"></a>

# 1. 概述
<a name="QwDWn"></a>

# 2. 菜单节点树
菜单节点树用于配置用户前端页面的路由树，类似传统的文件系统目录树，每个节点即一个页面。<br />![image.png](./pictures/1648179562674-f331a6a2-1e88-4553-93bd-e6448399033c.png)<br />**菜单路径: **用户访问页面时候在浏览器中会键入的URL段，一般由字母、数字、下划线、横杠组成。

<a name="HwDBY"></a>

# 3，页面设计器
<a name="lJw9W"></a>

## 3.1 主页面
<a name="QQROU"></a>

### 3.1.1 页面布局
**流式布局:**<br />**自定义布局:**
<a name="c4RGA"></a>

### 3.1.2 页面设置
**源JSON**<br />**变量管理**
<a name="sARzK"></a>

### 3.1.3 页面元素
<a name="kLo3f"></a>

### 3.1.4 页面数据源
<a name="oNmeR"></a>

## 3.2 页面区块
<a name="aPjSc"></a>

### 3.2.1 区块布局
<a name="lKYpa"></a>

### 3.2.2 区块设置
<a name="dLQAg"></a>

### 3.2.3 区块元素
<a name="fvlJK"></a>

### 3.2.4 区块数据源
<a name="XpfUI"></a>

# 4，元素编辑器
<a name="MzfAe"></a>

## 4.1 通用属性
<a name="dC3i8"></a>

## 4.2 卡片包装器
<a name="j9try"></a>

## 4.3 业务属性
<a name="kgjCU"></a>

## 4.4 组件数据源
<a name="vPZIl"></a>

# 5，数据源配置
<a name="AVJuc"></a>

## 5.1 API
<a name="MOSTs"></a>

## 5.2 JSON Data
<a name="PNusu"></a>

## 5.3 常量
<a name="dRTlF"></a>

## 5.4 函数
<a name="RAw9y"></a>

## 5.5 数据集
<a name="Pegi0"></a>

# 6，常见配置参考
<a name="LiVXg"></a>

## 6.1 操作配置
<a name="kRTEF"></a>

### 6.1.1 通用属性

<a name="fDnms"></a>

#### 前置函数
定义打开Action前的用户业务逻辑，可以进行Action初始化参数转换/是否可操作等信息提示。<br />节点参数：

   - nodeParams 节点参数域

返回值对象含义：

   - pass 是否可以启动操作；
   - type: 提示信息类型 值为info/warn/error；
   - message 提示信息,支持jsx；
   - transformParams:表单初始化值对象,可进行参数赋值覆盖等；
   - dynamicItems 动态表单集合定义,可动态控制表单项可见/新增/只读等；
   - width 自定对话框宽度
```js

function beforeHandler(nodeParams){
  //todo 用户逻辑
  return {type:"",message:"",title:"",pass:true,transformParams:false,dynamicItems:[]}
}
```
<a name="V1Xej"></a>

#### 提交前置函数
提交前对提交的数据进行处理函数<br />节点参数：

   - nodeParams 节点参数域
   - formValues表单参数对象；

返回值对象含义：

   - pass 是否可以提交；
   - type: 提示信息类型 值为info/warn/error；
   - message 提示信息,支持jsx；
   - transformParams:表单提交值转换,可进行赋值覆盖追加等；
```js

function beforeSubmitHandler(nodeParams,formValues){
  //todo 用户逻辑
  return {type:"",message:"",title:"",pass:true,transformParams:{}}
}
```
<a name="DKhug"></a>

#### 动作提示函数
动态提示，函数需要返回JSX字符串<br />节点参数：

   - nodeParams 节点参数域
   - formValues表单参数对象；

返回值对象含义：

   - 函数需要返回JSX字符串
```js

function hintHandler(nodeParams,formValues){
  //todo 用户逻辑
  return ""
}
```
<a name="adiz4"></a>

### 6.1.2 操作表单项

<a name="y3efF"></a>

#### 基础定义

<a name="U2VNY"></a>

#### 高级配置
<a name="CbFGW"></a>

#### 联动函数
```js

xxxx
```
<a name="CvhPo"></a>

## 6.2 过滤器配置
<a name="IEcnw"></a>

### 6.2.1 通用属性
<a name="Xhqvc"></a>

### 6.2.2 过滤表单项
<a name="oJJAL"></a>

## 6.3 表单项配置参考
<a name="sPgBY"></a>

### 6.3.1 表单项基础配置——基础定义
![image.png](./pictures/1648179562836-f47414e9-be6d-4415-8b6a-6ace0973505d.png)
<a name="CqVeJ"></a>

### 6.3.2 表单项高级配置——参数JSON配置
<a name="MkoHk"></a>

#### 分组配置
在高级配置中配置category后，可使得表单项分组管理<br />{<br />  "layout": {<br />    "category": "POC信息",<br />    "span": 24<br />  }<br />}
<a name="PuXow"></a>

#### Label和value配置
{<br />  "defaultValue": "brief",<br />  "options": [<br />    {<br />      "value": "brief",<br />      "label": "居中对齐"<br />    },<br />    {<br />      "value": "default",<br />      "label": "左对齐"<br />    },<br />    {<br />      "value": "empty",<br />      "label": "无菜单"<br />    }<br />  ]<br />}

<a name="KDj0I"></a>

### 6.3.3 常见表单项配置参考

![image.png](./pictures/1648179563059-2e368264-bfea-4b6e-8003-78ebaa9ff2f7.png)![image.png](./pictures/1648179563325-16eb655f-3a1c-4a82-8d10-a8106d2b69b7.png)<br />![image.png](./pictures/1648179563545-8fd4e110-bc57-426e-89b9-2aff03178b8a.png)![image.png](./pictures/1648179563746-99ede3d5-1631-42b7-b93e-f31509d4b57d.png)

<a name="J8InT"></a>

#### 可编辑tag组
可实现添加标签按钮的显示文案定制
```json

{
  "newTagLabel": "新建变量"
}
```
<a name="qX1qy"></a>

#### 机器选择器
<a name="ytGyb"></a>

#### 普通输入
可实现常见的antd的参数定制
```json

{
  "newTagLabel": "新建变量"
}
addonAfter	带标签的 input，设置后置标签	ReactNode	-	
addonBefore	带标签的 input，设置前置标签	ReactNode	-	
allowClear	可以点击清除图标删除内容	boolean	-	
bordered	是否有边框	boolean	true	4.5.0
defaultValue	输入框默认内容	string	-	
disabled	是否禁用状态，默认为 false	boolean	false	
id	输入框的 id	string	-	
maxLength	最大长度	number	-	
prefix	带有前缀图标的 input	ReactNode	-	
size	控件大小。注：标准表单内的输入框大小限制为 large	large | middle | small	-	
suffix	带有后缀图标的 input	ReactNode	-	
type	声明 input 类型，同原生 input 标签的 type 属性，见：MDN(请直接使用 Input.TextArea 代替 type="textarea")	string	text	
value	输入框内容	string	-	
onChange	输入框内容变化时的回调	function(e)	-	
onPressEnter	按下回车的回调	function(e)	-
```
<a name="pqAZm"></a>

#### 文本输入
<a name="djStx"></a>

#### 下拉单选
<a name="GZnfU"></a>

#### 下拉多选
<a name="mQWrp"></a>

#### 日期选择
<a name="PhArS"></a>

#### 日期范围
<a name="McU76"></a>

#### 可输入标签
<a name="KhmQG"></a>

#### 选择树
<a name="XUPo1"></a>

#### Radio单选
<a name="OQupY"></a>

#### CheckBox多选
<a name="DiHKU"></a>

#### 时间选择
<a name="Q5fYV"></a>

#### Radio按钮
<a name="RbGg5"></a>

#### Slider滑条
<a name="xIkWH"></a>

#### Switch开关
<a name="MfFC8"></a>

#### 级联单选
<a name="p9UX6"></a>

#### 密码输入
<a name="q5We2"></a>

#### 人员选择
<a name="hM7Zj"></a>

#### 分组输入
<a name="NevVr"></a>

#### JSONEditor
<a name="Iyi5n"></a>

#### Table
可编辑/新增/删除 表格
```yaml
{
  "enableRemove": true,
  "columns": [
    {
      "editProps": {
        "name": "key",
        "label": "",
        "required": false,
        "inputTip": "key",
        "defModel": {
        "remote":true,
        "optionMapping":{
            "label":"alias",
            "value":"name"
         }
       },
       "api":"gateway/sreworks-job/task/list?page=1&pageSize=9999999",
       "type":3
      },
      "dataIndex": "key",
      "title": "任务名称",
      "key": "key"
    },
    {
      "editProps": {
        "name": "value",
        "label": "",
        "type": 1,
        "required": false,
        "inputTip": "value"
      },
      "dataIndex": "value",
      "title": "任务序号",
      "key": "value"
    }
  ],
  "enableAdd": true,
  "enableEdit": true
}
```


<a name="w3ifw"></a>

#### 关联分组
<a name="NTXL9"></a>

#### ACEview
<a name="ViGIH"></a>

#### OamWidget（业务组件）内置业务组件
<a name="LljzT"></a>

#### 区块选择器（新增）
<a name="wwltD"></a>

#### 文件上传（需更新）
<a name="oAKdY"></a>

#### 上传图片
<a name="WvfJC"></a>

#### SchemaForm
<a name="NOzfy"></a>

#### 脚本气泡卡片
<a name="UEaxU"></a>

#### 卡片选择

<a name="dqdzx"></a>

# 7 前端组件扩展
[https://yuque.antfin.com/abm/cq9dg6/ngx61f](https://yuque.antfin.com/abm/cq9dg6/ngx61f)
<a name="f799b"></a>

## 7.1 基础组件扩展

<a name="q8b31"></a>

## 7.2 布局组件扩展

<a name="puEHY"></a>

## 7.3 业务组件扩展

<a name="QA7D5"></a>

## 7.4 表单项扩展

<a name="u5EKp"></a>

# 8 常见前端知识
<a name="eWp5c"></a>

## 8.1 JSX
