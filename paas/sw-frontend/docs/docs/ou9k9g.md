---
title: 4.2.3 运维前端开发
date: 2022-06-20T10:37:29.000Z
---

<a name="Zelez"></a>

# 1. 概述
sw-frontend工程采用React+antd为主的技术框架，设计了一套组件映射、编排、解析、渲染的工程体系：以antd组件为自由编辑粒度，用户在前端设计器通过可视化交互或者json编辑的方式，依据运维工作的实际使用场景，对组件进行属性配置/组件嵌套拼装；同时根据运维场景目标需求对页面组件进行布局的编排、数据源的绑定以及在合适点位插入Dynamic Logic，完成页面节点的设计工作，形成节点模型nodeModel，经模板解析引擎进行解析渲染。<br />sw-frontend整体架构设计图如下：<br />![image.png](./pictures/1655721470100-56c612e7-9138-45de-9108-ca30db5f7389.png)


<a name="HwDBY"></a>

# 2. 页面设计器
**swadmin页面设计器**作为内置应用的一员，同时也是其他应用的母应用，一生万物，所有应用的都是通过前端设计器设计配置而来；在此可以对组件/页面进行业务字段、ui和数据源的配置，对当前应用的节点树进行编辑，以及对每级节点对应页面的组件进行属性配置、数据源绑定和布局编排工作 。<br />其中设计器左侧菜单树，可以进行路由的设计、角色的设定、子路由菜单的布局与展现方式、页面布局类型(流式布局/自定义布局)等，右侧画布区用于页面的组件设计和编排。<br />页面设计器提供两种方式进行页面的设计与编排：可视化编辑，直观易用；JSON源码编辑，增加灵活度。<br />![image.png](./pictures/1655721470331-a5b7189f-62a4-4db1-9da8-055bb5ee1a9a.png)<br />![image.png](./pictures/1655721470476-7266e4d9-cd13-421a-8402-4553c5d4e056.png)
<a name="VdK4R"></a>

# 3.菜单树与路由
sw-frontend以用户创建的各个应用为一级路由构建起整个前端工程的菜单体系，所有的页面都是依托“应用”为节点进行挂载的。下图为应用**运维桌面**，用户可以点击应用快捷方式进入运维应用<br />![image.png](./pictures/1655721470818-d8b86f3c-f775-4a77-a424-19695519cc3d.png)<br />**运维桌面**同时也是我们应用维度的其中一员，即在sw-frontend的总体设计概念上：一切皆是应用<br />![image.png](./pictures/1655721471248-b15971e5-295b-4ad4-b392-e4e6b57b9ad4.png)<br />每个应用的页面菜单都抽象为一个页面节点树nodeTree，并在Node节点上可以对节点类型和菜单页布局以及权限等信息进行配置(菜单/Link作为嵌套路由使用)。我们将挂在在该节点的主页面和页面区块的相关配置：属性、数据源、编排等进行配置，我们称之为节点配置（nodeConfig）<br />![image.png](./pictures/1655721472349-c20294c2-5adc-4e44-a495-bc9b3c826aad.png)
<a name="Zubx9"></a>

# 4. 数据源配置
组件数据源目前支持三种方式： API接口调用、JSON写入与函数传入
<a name="AVJuc"></a>

## 4.1 API
API类型的数据源可以对接口进行协议方式的定义，请求参数的定义与混入，以及提交前置函数的使用，可以灵活处理提交参数的格式或添加一些业务逻辑，同时支持runtime参数的引用，具体格式为$(变量名)。runtime参数在节点参数阈中完成替换。<br />![image.png](./pictures/1655721474767-9e9f6771-4143-4ccb-b93c-e4352b8c658a.png)

<a name="MOSTs"></a>

## 4.2 JSON
JSON类型的数据源可以直接将JSON格式的文件通过设计器写入，并传递到组件中。<br />![image.png](./pictures/1655721474410-41410184-b881-4c09-87d0-0192653d2cb6.png)
<a name="dRTlF"></a>

## 4.3 函数
函数类型的数据源跟JSON类似，在此可以嵌入一定的业务逻辑，函数返回的JSON数据会被注入到组件中。<br />![image.png](./pictures/1655721474596-55733159-792f-455d-84c1-941e24be2016.png)
<a name="rzPOE"></a>

# 5 函数
<a name="nyc2v"></a>

## 5.1 前置函数
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
<a name="gAnzc"></a>

## 5.2 提交前置函数
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
<a name="sXcYk"></a>

## 5.3 动作提示函数
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
<a name="dh8pm"></a>

# 6 操作表单项

<a name="t9b8b"></a>

## 6.1基础定义
![image.png](./pictures/1655721474789-2901ab8f-407a-46d6-96a5-4e458138965a.png)操作表单项支持动态添加和删除表单项，对于表单项的定义，提供表单label、key、表单呈现形式(input,select等三十余种类型)、默认值、正则校验、tooltip提示、是否可见表达式、以及是否只读等自定义编辑项。对于表单项的顺序，提供拖拽排序能力。
<a name="SJoR8"></a>

## 6.2 表单项数据源

- 数据源

![image.png](./pictures/1655721474786-40f2c52f-d7db-4433-bbf9-e694c69b3246.png)<br />对于select，可编辑表格等表单项，提供待选列表项数据源接入配置的能力，并可以根据实际业务场景在提交前置和后置函数中修改数据结构以及嵌入业务逻辑。

- 高级配置

<a name="M8t0i"></a>

## 6.3 高级配置和联动函数
![image.png](./pictures/1655721476692-a287ad61-76ce-4a6a-8128-e435cea5e321.png)

- 高级配置

高级配置应用于一些复杂表单项组件，如table、可以在此配置:

   1. table是否可编辑: 
```json

{"enableEdit": true}
```

   2. table单选: 
```json

{"enableSelected": true}
```

   3. table多选:
```json

{"enableSelected": true,"multiple": true}
```

- 联动函数

表单项值更改后,引起其他表单项值更改的处理函数,入参为当前表单所有值及节点参数域,返回值为影响的表单值对象, 可在effectHandle函数，根据nodeParams和formValues进行业务逻辑的写入与执行。
<a name="ihPPH"></a>

# 7 组件扩展

- JSXRender自定义组件

sw-frontend提供了声明式内置组件的注册机制，同时开辟了用户自定义组件的入口，用户可以将一些常用的自定义组件嵌入到我们的组件列表，以供前端设计器统一调拨；当前自定义组件以JSXRender的方式进行执行，可以直接书写JSX代码，同时能够识别antd组件，赋予用户更加灵活的页面渲染能力。<br />![image.png](./pictures/1655721477777-7c66fe35-6378-4517-a783-9dfea665071c.png)

- umd远程加载组件，脚手架尚未发布(已排上日程)
<a name="HpSnA"></a>

# 8 组件配置参考
<a name="jwoZ8"></a>

### 8.1 **复杂列表**
<a name="SvA6u"></a>

#### 数据源/数据结构
![image.png](./pictures/1655721481971-55bd0698-0192-4240-91d8-2a5aaf829015.png)
```js

{
  "page": 1,
    "pageSize": 20,
      "total": 1,
        "items": [
          {
            "gmtModified": 1645428289000,
            "appId": "swadmin",
            "addonType": "INTERNAL_ADDON",
            "addonVersion": "1.0.0",
            "name": "productopsv2",
            "id": 143,
            "gmtCreate": 1645428289000,
            "addonId": "productopsv2",
            "spec": {}
          }
        ],
          "empty": false
}
```

<a name="z2QMn"></a>

### 8.2**网格卡片**
<a name="MAUG7"></a>

#### 数据源格式
![image.png](./pictures/1655721477811-03eda501-93ca-4b57-881d-43491c812017.png)<br />默认的column如下：
```json

"columns": [
  {
    "dataIndex": "name",
    "label": "姓名"
  },
  {
    "dataIndex": "age",
    "label": "年龄"
  }
                ],
```
默认的数据源数据如下：
```json

[
  {
    "age": "111",
    "icon": "https://zos.alipayobjects.com/rmsportal/ODTLcjxAfvqbxHnVXCYX.png",
    "name": "张三",
    "title": "测试测试测试"
  },
  {
    "age": "112",
    "icon": "https://zos.alipayobjects.com/rmsportal/ODTLcjxAfvqbxHnVXCYX.png",
    "name": "李四",
    "title": "测试测试测试"
  }
            ]
```
网格卡片组件的column配置和footer部分的action暂时需要在源码编辑模式下进行编辑：<br />![image.png](./pictures/1655721481913-a0620dc1-12e5-4c19-8ef7-a91dbc45f278.png)
<a name="r7tG2"></a>

#### 组件属性可视化配置

- 布局式样: 网格卡片提供了两种布局式样
- 分页：是否进行后端分页
<a name="jsU9R"></a>

### 8.3 统计卡片
<a name="z6isL"></a>

#### 数据源

```json

{
  "value":200,
  "title": "title",
  "data":[
      {
        "value": 28,
        "title": "占有率",
        "status": "success",
        "trend": "up",
        "suffix": "万",
        "icon": "",
        "link":"xxxxxxxx"//若要添加link事件,则在此配置相应链接即可
      },
      {
        "value": 28,
        "title": "占有率",
        "status": "success",
        "trend": "down",
        "suffix": "万",
        "icon": "",
        "link": "href"
      },
      {
        "value": 28,
        "title": "占有率",
        "status": "success",
        "trend": "up",
        "suffix": "万",
        "icon": ""
      }
    ]
}

```
<a name="HL3a5"></a>

#### 组件属性可视化配置
![image.png](./pictures/1655721479134-7ecc9858-ef80-42c8-abe4-2df04c458f12.png)

- 标题icon: 用户可以自定义上传左上侧icon
- 卡片边框：是否展示边框
- 卡片icon：效果等同于标题icon，优先级低于本地上传icon
- 卡片宽度：number类型，设置卡片宽度
- 卡片高度: number类型，设置卡片高度
<a name="uRj1p"></a>

### 8.4 水波图

<a name="APtlW"></a>

#### 数据源结构
```json

2600
```
![image.png](./pictures/1655721481860-251c252b-21a9-41c1-9b6a-286d8e002da6.png)<br />此处26%的比例来源：2600/10000,10000为水波图的默认分母值(最大值-最小值)，这个重量数值可以在水波图的可视化字段配置处修改。
<a name="xwXNe"></a>

#### 组件属性可视化配置
![image.png](./pictures/1655721481198-a6e77349-42fb-4f03-a6fc-a50563a09ec5.png)

- 主题：本白/亮黑
- 高度：图表渲染容器的高度，number类型
- 宽度：图表渲染容器的宽度，number类型
- 刷新周期：未定义时，则初始化时拉取数据；配置了刷新周期后，则按周期间隔刷新数据，number类型
- 边距：图表渲染容器的padding值，格式"10,0,0,10",对应上、右、下、左
- 标题：水波图的内置标题说明
- 刻度最小值：水波的上部预留空间值
- 刻度最大值：水波容器的最大值
- 描述：水波值得数字描述说明
- 图表颜色：水波图的颜色，可以选定为任意色，在此处为色盘
- 自定义配置：在此可以配置水波图的自定义高级配置，如事件绑定、颜色、自定义描述、自定义图例等，具体可参考bizcharts：[https://bizcharts.taobao.com/product/BizCharts4/category/77/page/144#max-%E2%9C%A8](https://bizcharts.taobao.com/product/BizCharts4/category/77/page/144#max-%E2%9C%A8)
```js

function advancedConfig(widgetData){
//widgetData为传入的数值
  return {
    statistic:{
				title: {
					customHtml(container, view, item) {
						return '比例'
					}
				},
				content: {
					style: {
						fill: "#000"
					},
					customHtml(container, view, item) {
						return `${(item.percent * 100).toFixed(2)}%`
					}
				}
  },
  //事件
  events:{
        onLiquidClick: (event) => console.log(event),
        onPlotClick: (event) => console.log(event),
        onStatisticClick: (event) => console.log(event),
    }
  }
}
```
<a name="DnEYg"></a>

### 8.5 基础条带图

<a name="CJy49"></a>

#### 数据源结构
基础条带图是条形图、折线图、柱状图和点图的选项集成，可以在组件属性出选择配置图表类型，相对应的数据源结构和bizCharts保持一致

- 折线图

![image.png](./pictures/1655721481739-7f158333-e1ff-4af0-af63-d92dc19aec81.png)
```json

[
  {
    "year": "1991",
    "value": 3
  },
  {
    "year": "1992",
    "value": 4
  },
  {
    "year": "1993",
    "value": 3.5
  },
  {
    "year": "1994",
    "value": 5
  },
  {
    "year": "1995",
    "value": 4.9
  },
  {
    "year": "1996",
    "value": 6
  },
  {
    "year": "1997",
    "value": 7
  },
  {
    "year": "1998",
    "value": 9
  },
  {
    "year": "1999",
    "value": 13
  }
]
```

- 柱状图

![image.png](./pictures/1655721482727-e572ee2d-9bc5-4c9f-bb9e-d912c2fb3a63.png)
```json

[
  {
    "year": "1991",
    "value": 3
  },
  {
    "year": "1992",
    "value": 4
  },
  {
    "year": "1993",
    "value": 3.5
  },
  {
    "year": "1994",
    "value": 5
  },
  {
    "year": "1995",
    "value": 4.9
  },
  {
    "year": "1996",
    "value": 6
  },
  {
    "year": "1997",
    "value": 7
  },
  {
    "year": "1998",
    "value": 9
  },
  {
    "year": "1999",
    "value": 13
  }
]
```

- 条形图
```json

[
      {
        year: "1991",
        value: 3,
      },
      {
        year: "1992",
        value: 4,
      },
      {
        year: "1993",
        value: 3.5,
      },
      {
        year: "1994",
        value: 5,
      },
      {
        year: "1995",
        value: 4.9,
      },
      {
        year: "1996",
        value: 6,
      },
      {
        year: "1997",
        value: 7,
      },
      {
        year: "1998",
        value: 9,
      },
      {
        year: "1999",
        value: 13,
      }
    ]
```

- 点图
```json

[
  {
    "year": "1991",
    "value": 3
  },
  {
    "year": "1992",
    "value": 4
  },
  {
    "year": "1993",
    "value": 3.5
  },
  {
    "year": "1994",
    "value": 5
  },
  {
    "year": "1995",
    "value": 4.9
  },
  {
    "year": "1996",
    "value": 6
  },
  {
    "year": "1997",
    "value": 7
  },
  {
    "year": "1998",
    "value": 9
  },
  {
    "year": "1999",
    "value": 13
  }
]
```
<a name="XaVTb"></a>

#### 组件可视化属性配置
通过查看对比以上四种类型的图表我们可以看出四种图表的数据结构是相同的，其可配置的属性字段也基本类似，尤其柱状图和条形图是只是在配置时交换了下X轴和Y轴的指定字段。<br />折线图：[https://bizcharts.taobao.com/product/BizCharts4/category/77/page/116](https://bizcharts.taobao.com/product/BizCharts4/category/77/page/116)<br />条形图：[https://bizcharts.taobao.com/product/BizCharts4/category/77/page/126](https://bizcharts.taobao.com/product/BizCharts4/category/77/page/126)<br />柱状图：[https://bizcharts.taobao.com/product/BizCharts4/category/77/page/120](https://bizcharts.taobao.com/product/BizCharts4/category/77/page/120)<br />点图：[https://bizcharts.taobao.com/product/BizCharts4/category/77/page/136](https://bizcharts.taobao.com/product/BizCharts4/category/77/page/136)<br />![image.png](./pictures/1655721484026-9b37b834-1376-46bb-aa14-e637c47a588a.png)

- 基础图表类别：条形图/柱状图/折线图/点图
- 主题：白色/亮黑
- 高度：定义渲染容器的高度，number类型，单位px
- 宽度：定义渲染容器的宽度，number类型，单位px
- 刷新周期：在未配置的情况下，默认初始化拉取数据，在定义了刷新周期后，则定时刷新数据，nunber类型
- 边距：渲染容器的的padding，格式[30,40,30,40],对应上、右、下、左、四个方位
- 标题：用于定义渲染区左上侧对图表的描述
- X轴字段： 指定数据源结构中用于X轴的字段
- Y轴字段： 指定数据源结构中用于Y轴的字段
- 分组字段：用于对柱状图/条形图/折线图等的分组展示，示例如下

![image.png](./pictures/1655721483811-b165f570-03f3-4017-a33f-ae9c609f83ef.png)<br />**此处指定了type为分组字段，value为X轴字段，label为Y轴字段**
```json

[
  {
    "label": "Mon.",
    "type": "series1",
    "value": 2800
  },
  {
    "label": "Mon.",
    "type": "series2",
    "value": 2260
  },
  {
    "label": "Tues.",
    "type": "series1",
    "value": 1800
  },
  {
    "label": "Tues.",
    "type": "series2",
    "value": 1300
  },
  {
    "label": "Wed.",
    "type": "series1",
    "value": 950
  },
  {
    "label": "Wed.",
    "type": "series2",
    "value": 900
  },
  {
    "label": "Thur.",
    "type": "series1",
    "value": 500
  },
  {
    "label": "Thur.",
    "type": "series2",
    "value": 390
  },
  {
    "label": "Fri.",
    "type": "series1",
    "value": 170
  },
  {
    "label": "Fri.",
    "type": "series2",
    "value": 100
  }
]
```

- 是否展示图例：图例的展示与隐藏
- 图例位置：图例的位置
- 缩放条：鼠标滚轮拖动缩放
- 自定义配置：widgetData为数据源数据，返回自定义配置对象，用于图表的高级配置，如事件绑定，颜色精准修改，legend自定义渲染等，也可以对以上可视化配置字段进行overwrite, 在优先级上高于可视化配置字段。具体配置可参考[https://bizcharts.taobao.com/product/BizCharts4/category/77/page/128](https://bizcharts.taobao.com/product/BizCharts4/category/77/page/128)
```js

function advancedConfig(widgetData){
  return {
    // 此处是对图表颜色的自定义配置
    color:['blue','yellow','green'],
    // 通过callback指定颜色
    colorField:'type',
    color:(d)=>{
        if(d==='a') return ['blue','yellow','green'];
        return ['blue','green','yellow'];
    },
    // tite的自定义配置
    title: {
        visible: false,
        alignTo: 'left',
        text:'',
        style:{
            fontSize: 18,
            fill: 'black',
        }
    },
    // 事件的定义
    events:{
        onBarClick: (event) => console.log(event),
        onLegendClick: (event) => console.log(event),
        onPlotClick: (event) => console.log(event),
    },Ï
  }
}
//此对象还可以添加更多的自定义配置
```
<a name="bTqKb"></a>

### 8.6 饼图

<a name="JpZEP"></a>

#### 数据源结构
![image.png](./pictures/1655721484019-7abe2d1e-d1ff-43f4-9c4d-256165800adf.png)<br />SREWorks中的图表是bizcharts的集成，数据结构类型跟bizCharts保持一致：[https://bizcharts.taobao.com/product/BizCharts4/category/77/page/130](https://bizcharts.taobao.com/product/BizCharts4/category/77/page/130).
```json

[
  { "item": "事例一", "percent": 0.4 },
  { "item": "事例二", "percent": 0.21 },
  { "item": "事例三", "percent": 0.17 },
  { "item": "事例四", "percent": 0.13 },
  { "item": "事例五", "percent": 0.09 }
]
```
<a name="V0f6F"></a>

#### 组件可视化属性配置
![image.png](./pictures/1655721484692-d5502677-cc07-4462-b1c8-3482d942bbba.png)

- x坐标轴字段：用户定义的横轴展示字段
- 高度和宽度：用来定义图表渲染区容器的边界范围
- 刷新周期： 在未配置的情况下，会初始化执行一次数据刷新，若配置了具体刷新周期(如5000)，则该图表会每隔5000毫秒进行间隔刷新。
- 边距：图表容器的padding值
- 分色字段：格式为颜色号数组(eg: ["blue","red","green",......]), 在未配置时为bizcharts初始颜色数组。
- 占比字段： 用于指定数据源数据，用于分配饼图比例的value字段，为number类型
- 半径比例： 定义饼图的半径，number类型，小于1
- 是否展示图例：图例的展示隐藏
- 图例位置：
- 自定义配置：属于高级配置，用于修改饼图的自定义渲染：如颜色、半径比、自定义图例说明，事件绑定等，详细用法同bizCharts一致，其中widgetData为传入数据源数据，返回值为对象类型。对于上面已定义的配置，在自定义配置中可以进行覆盖，自定义配置的优先级更高。[https://bizcharts.taobao.com/product/BizCharts4/category/77/page/130](https://bizcharts.taobao.com/product/BizCharts4/category/77/page/130)
```js

function advancedConfig(widgetData){
  return {
    // 此处是对图表颜色的自定义配置
    color:['blue','yellow','green'],
    // 通过callback指定颜色
    colorField:'type',
    color:(d)=>{
        if(d==='a') return ['blue','yellow','green'];
        return ['blue','green','yellow'];
    },
    // tite的自定义配置
    title: {
        visible: false,
        alignTo: 'left',
        text:'',
        style:{
            fontSize: 18,
            fill: 'black',
        }
    },
    // 事件的定义
    events:{
        onBarClick: (event) => console.log(event),
        onLegendClick: (event) => console.log(event),
        onPlotClick: (event) => console.log(event),
    },Ï
  }
}
//此对象还可以添加更多的自定义配置
```
<a name="k9IQr"></a>

### 8.7 表格

<a name="okQN8"></a>

#### 数据源结构
后端分页结构
```json

{
  "page": 1,
  "pageSize": 47,
  "total": 47,//在后端进行分时，total字段
  "items": [
    {
      // "gmtCreate\appId等和table定义的column字段对应"
      "gmtCreate": 1630597034000,
      "gmtModified": 1630597034000,
      "appId": "desktop",
      "options": {
        "layout": {
          "type": "empty"
        },
        "logoImg": "/static/publicMedia/desktop.png",
        "swapp": "apiVersion: core.oam.dev/v1alpha2\nkind: ApplicationConfiguration\nmetadata:\n  name: deploy-desktop-package\n  annotations:\n    appId: desktop\n    clusterId: master\n    namespaceId: sreworks\n    stageId: prod\nspec:\n  components:\n  - dataInputs: []\n    dataOutputs: []\n    dependencies: []\n    revisionName: INTERNAL_ADDON|productopsv2|_\n    scopes:\n    - scopeRef:\n        apiVersion: flyadmin.alibaba.com/v1alpha1\n        kind: Namespace\n        name: sreworks\n    - scopeRef:\n        apiVersion: flyadmin.alibaba.com/v1alpha1\n        kind: Stage\n        name: 'prod'\n    - scopeRef:\n        apiVersion: flyadmin.alibaba.com/v1alpha1\n        kind: Cluster\n        name: 'master'\n    parameterValues:\n    - name: TARGET_ENDPOINT\n      value: \"prod-flycore-paas-action\"\n      toFieldPaths:\n        - spec.targetEndpoint\n    - name: STAGE_ID\n      value: 'prod'\n      toFieldPaths:\n        - spec.stageId\n  - dataInputs: []\n    dataOutputs: []\n    dependencies: []\n    revisionName: INTERNAL_ADDON|appmeta|_\n    scopes:\n    - scopeRef:\n        apiVersion: flyadmin.alibaba.com/v1alpha1\n        kind: Namespace\n        name: sreworks\n    - scopeRef:\n        apiVersion: flyadmin.alibaba.com/v1alpha1\n        kind: Stage\n        name: 'prod'\n    - scopeRef:\n        apiVersion: flyadmin.alibaba.com/v1alpha1\n        kind: Cluster\n        name: 'master'\n    parameterValues:\n    - name: STAGE_ID\n      value: 'prod'\n      toFieldPaths:\n        - spec.stageId",
        "docsUrl": "",
        "builtIn": 1,
        "name": "运维桌面",
        "description": "桌面",
        "source": "swadmin",
        "category": "管理",
        "version": "v2"
      },
      "environments": [
        {
          "clusterId": "master",
          "namespaceId": "sreworks",
          "stageId": "prod"
        }
      ]
    }
  ],
}
```
非后端分页结构
```json

[
    {
      // "gmtCreate\appId等和table定义的column字段对应"
      "gmtCreate": 1630597034000,
      "gmtModified": 1630597034000,
      "appId": "desktop",
      "options": {
        "layout": {
          "type": "empty"
        },
        "logoImg": "/static/publicMedia/desktop.png",
        "swapp": "apiVersion: core.oam.dev/v1alpha2\nkind: ApplicationConfiguration\nmetadata:\n  name: deploy-desktop-package\n  annotations:\n    appId: desktop\n    clusterId: master\n    namespaceId: sreworks\n    stageId: prod\nspec:\n  components:\n  - dataInputs: []\n    dataOutputs: []\n    dependencies: []\n    revisionName: INTERNAL_ADDON|productopsv2|_\n    scopes:\n    - scopeRef:\n        apiVersion: flyadmin.alibaba.com/v1alpha1\n        kind: Namespace\n        name: sreworks\n    - scopeRef:\n        apiVersion: flyadmin.alibaba.com/v1alpha1\n        kind: Stage\n        name: 'prod'\n    - scopeRef:\n        apiVersion: flyadmin.alibaba.com/v1alpha1\n        kind: Cluster\n        name: 'master'\n    parameterValues:\n    - name: TARGET_ENDPOINT\n      value: \"prod-flycore-paas-action\"\n      toFieldPaths:\n        - spec.targetEndpoint\n    - name: STAGE_ID\n      value: 'prod'\n      toFieldPaths:\n        - spec.stageId\n  - dataInputs: []\n    dataOutputs: []\n    dependencies: []\n    revisionName: INTERNAL_ADDON|appmeta|_\n    scopes:\n    - scopeRef:\n        apiVersion: flyadmin.alibaba.com/v1alpha1\n        kind: Namespace\n        name: sreworks\n    - scopeRef:\n        apiVersion: flyadmin.alibaba.com/v1alpha1\n        kind: Stage\n        name: 'prod'\n    - scopeRef:\n        apiVersion: flyadmin.alibaba.com/v1alpha1\n        kind: Cluster\n        name: 'master'\n    parameterValues:\n    - name: STAGE_ID\n      value: 'prod'\n      toFieldPaths:\n        - spec.stageId",
        "docsUrl": "",
        "builtIn": 1,
        "name": "运维桌面",
        "description": "桌面",
        "source": "swadmin",
        "category": "管理",
        "version": "v2"
      },
      "environments": [
        {
          "clusterId": "master",
          "namespaceId": "sreworks",
          "stageId": "prod"
        }
      ]
    },
    {
      // "gmtCreate\appId等和table定义的column字段对应"
      "gmtCreate": 1630597034000,
      "gmtModified": 1630597034000,
      "appId": "desktop",
      "options": {
        "layout": {
          "type": "empty"
        },
        "logoImg": "/static/publicMedia/desktop.png",
        "swapp": "apiVersion: core.oam.dev/v1alpha2\nkind: ApplicationConfiguration\nmetadata:\n  name: deploy-desktop-package\n  annotations:\n    appId: desktop\n    clusterId: master\n    namespaceId: sreworks\n    stageId: prod\nspec:\n  components:\n  - dataInputs: []\n    dataOutputs: []\n    dependencies: []\n    revisionName: INTERNAL_ADDON|productopsv2|_\n    scopes:\n    - scopeRef:\n        apiVersion: flyadmin.alibaba.com/v1alpha1\n        kind: Namespace\n        name: sreworks\n    - scopeRef:\n        apiVersion: flyadmin.alibaba.com/v1alpha1\n        kind: Stage\n        name: 'prod'\n    - scopeRef:\n        apiVersion: flyadmin.alibaba.com/v1alpha1\n        kind: Cluster\n        name: 'master'\n    parameterValues:\n    - name: TARGET_ENDPOINT\n      value: \"prod-flycore-paas-action\"\n      toFieldPaths:\n        - spec.targetEndpoint\n    - name: STAGE_ID\n      value: 'prod'\n      toFieldPaths:\n        - spec.stageId\n  - dataInputs: []\n    dataOutputs: []\n    dependencies: []\n    revisionName: INTERNAL_ADDON|appmeta|_\n    scopes:\n    - scopeRef:\n        apiVersion: flyadmin.alibaba.com/v1alpha1\n        kind: Namespace\n        name: sreworks\n    - scopeRef:\n        apiVersion: flyadmin.alibaba.com/v1alpha1\n        kind: Stage\n        name: 'prod'\n    - scopeRef:\n        apiVersion: flyadmin.alibaba.com/v1alpha1\n        kind: Cluster\n        name: 'master'\n    parameterValues:\n    - name: STAGE_ID\n      value: 'prod'\n      toFieldPaths:\n        - spec.stageId",
        "docsUrl": "",
        "builtIn": 1,
        "name": "运维桌面",
        "description": "桌面",
        "source": "swadmin",
        "category": "管理",
        "version": "v2"
      },
      "environments": [
        {
          "clusterId": "master",
          "namespaceId": "sreworks",
          "stageId": "prod"
        }
      ]
    }
  ]
```
<a name="CCWs5"></a>

#### 操作
工具栏(toolbar)： 当table的通用配置选择外部包装器选项，则支持工具栏选项，在此处可以添加操作按钮<br />![image.png](./pictures/1655721485107-e50aa050-01e4-42bd-8585-c5878d53b8d8.png)<br />行内操作（itemToolbar）：   行内操作如“行编辑”等需要在数据项工具栏处定义，并关联操作区块<br />![image.png](./pictures/1655721485730-18eacc73-f22a-42c1-bcb8-19b09fd79c30.png)<br />行内超链接/添加icon等，以column定义区添加render的方式进行，其中row字段为目标行数据的传入参数对象，可供render处调用。方式如下图
```js

<a href='/#/dataops/dw/model_details?layer=ads&subject=service&modelName=$(row.name)&modelId=$(row.id)'>$(row.name)</a>
```
<a name="SkcZd"></a>

#### 高级配置

- 折叠子表格

表格的折叠子表格的配置，目前暂只支持源码编辑的方式配置，在表格组件的一级字段下添加“expandedRow”配置：
```json

"expandedRow": {
    "name": "app_comp_list",
    "type": "TABLE",
    "config": {
      "rowActions": {
        "layout": "",
        "fixed": false,
        "type": "link",
        "actions": [
          {
            "icon": "reload",
            "name": "retry_package",
            "label": "重试",
            "btnType": "primary",
            "hiddenExp": "true && row.taskStatus!=='FAILURE'"
          },
          {
            "icon": "file-search",
            "name": "view_log_package2",
            "label": "查看日志",
            "btnType": "primary",
            "hiddenExp": "true "
          }
        ]
      },
      "columns": [
        {
          "dataIndex": "id",
          "label": "组件构建ID"
        },
        {
          "dataIndex": "identifier",
          "label": "组件标识"
        },
        {
          "dataIndex": "deployStatus",
          "label": "状态"
        },
        {
          "dataIndex": "gmtCreate",
          "label": "创建时间",
          "render": "<common.DateTime  value={row.gmtCreate}></common.DateTime>"
        },
        {
          "dataIndex": "gmtModified",
          "label": "更新时间",
          "render": "<common.DateTime  value={row.gmtModified}></common.DateTime>"
        }
      ],
      "dataIndex": "deployComponents",
      "api": {
        "paging": false,
        "url": "gateway/v2/foundation/appmanager/deployments/$(id)"
      },
      "title": "部署详情"
    }
  },
```
渲染效果如下：<br />![image.png](./pictures/1655721485718-713226da-c087-4c24-b89b-d5c02bdd7df4.png)
<a name="k1L82"></a>

#### 表单项内表格配置
![image.png](./pictures/1655721486753-75674b05-826c-438c-8277-650f6519d905.png)<br />表单项内表格支持，单选/多选/动态添加行等配置

