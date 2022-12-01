/**
 * Created by caoshuaibiao on 2021/2/1.
 */
import table from './icons/table.svg'
export default {
  id: 'TABLE',
  type: 'TABLE',
  name: 'TABLE',
  title: '表格',
  info: {
    author: {
      name: '',
      url: '',
    },
    description: '通用表格',
    links: [],
    logos: {
      large: '',
      small: table,
      fontClass: 'TABLE',
    },
    build: {
      time: '',
      repo: '',
      branch: '',
      hash: '',
    },
    screenshots: [],
    updated: '',
    version: '',
    docs:
      "<div><a target='_blank' href='#/help/book/documents/ho617k.html#87-表格'>table配置</a></div><br />" +
      "<div><a target='_blank' href='https://ant.design/components/table-cn/#header'>列完整配置见antd Table Column 配置</a></div>",
  },
  state: '',
  latestVersion: '1.0',
  configSchema: {
    defaults: {
      config: {
        api: {
          url: '',
          paging: false,
        },
        columns: [
          {
            dataIndex: 'label',
            filters: [
              {
                text: 'Joe',
                value: '1',
              },
              {
                text: 'Jim',
                value: '8',
              },
              {
                text: 'Submenu',
                children: [
                  {
                    text: 'Green',
                    value: 'Green',
                  },
                  {
                    text: 'Black',
                    value: 'Black',
                  },
                ],
                value: 'Submenu',
              },
            ],
            label: 'label',
          },
          {
            dataIndex: 'value',
            render: "<a href='$(row.label)'>$(row.value)</a>",
            label: 'value',
          },
          {
            defaultSortOrder: 'ascend',
            dataIndex: 'number',
            label: '编号',
          },
        ],
        title: '表格',
        size: 'small',
      },
      type: 'TABLE',
    },
    schema: {
      type: 'object',
      properties: {
        columns: {
          description: '列定义,更多高级设置参考配置文档',
          title: '列定义',
          required: false,
          'x-component': 'EditTable',
          type: 'string',
          enableScroll: true,
          'x-component-props': {
            columns: [
              {
                editProps: {
                  required: false,
                  type: 1,
                  inputTip: '列头',
                },
                dataIndex: 'label',
                title: '列头',
              },
              {
                editProps: {
                  required: false,
                  inputTip: '值索引',
                  type: 1,
                },
                dataIndex: 'dataIndex',
                title: '值索引',
              },
              {
                editProps: {
                  required: false,
                  type: 1,
                  inputTip: '自定义列渲染内容,$(row.xxx),来获取行数据,支持系统内置render',
                },
                dataIndex: 'render',
                title: 'render',
                width: '200',
                textWrap: 'word-break',
              },
              {
                editProps: {
                  required: false,
                  inputTip: '列宽,支持百分比',
                  type: 1,
                },
                dataIndex: 'width',
                title: '列宽',
              },
            ],
          },
        },
        size: {
          description: '设定表格的size大小',
          title: 'size',
          required: false,
          type: 'string',
          'x-component': 'Radio',
          'x-component-props': {
            options: [
              { value: 'small', label: 'small' },
              { value: 'middle', label: 'middle' },
              { value: 'default', label: 'large' },
            ],
            defaultValue: 'small',
          },
        },
        paging: {
          description: '设定表格分页,配置分页需要在请求参数中添加分页参数',
          title: '分页',
          required: false,
          type: 'string',
          'x-component': 'Radio',
          initValue: false,
          'x-component-props': {
            options: [
              { value: true, label: '是' },
              { value: false, label: '否' },
            ],
          },
        },
        bordered: {
          description: '表格是否带有外框和竖边框',
          title: '是否边框',
          type: 'string',
          required: false,
          'x-component': 'Radio',
          initValue: true,
          'x-component-props': {
            options: [
              { value: true, label: '是' },
              { value: false, label: '否' },
            ],
          },
        },
        rowColorMapping: {
          description: '根据行数据中的值显示指定颜色,支持green,blue,red,yellow和themeColor五种颜色',
          title: '行颜色定义',
          required: false,
          initValue: { dataIndex: 'key', mapping: { value1: 'green' } },
          'x-component': 'JSON',
          type: 'string',
        },
        emptyText: {
          description: '支持空数据自定义文案',
          title: '空数据文案',
          required: false,
          'x-component': 'Text',
          initValue: '',
          type: 'string',
        },
      },
    },
    supportItemToolbar: true,
    supportToolbar: true,
    dataMock: {
      description: '返回的数据为对象',
      formats: [
        {
          description: '后端分页数据结构',
          data: {
            page: 1,
            pageSize: 47,
            total: 47, //在后端进行分时，total字段
            items: [
              {
                // "gmtCreate\appId等和table定义的column字段对应"
                gmtCreate: 1630597034000,
                gmtModified: 1630597034000,
                appId: 'desktop',
                options: {
                  layout: {
                    type: 'empty',
                  },
                  logoImg: '/static/icons/desktop.png',
                  swapp:
                    "apiVersion: core.oam.dev/v1alpha2\nkind: ApplicationConfiguration\nmetadata:\n  name: deploy-desktop-package\n  annotations:\n    appId: desktop\n    clusterId: master\n    namespaceId: sreworks\n    stageId: prod\nspec:\n  components:\n  - dataInputs: []\n    dataOutputs: []\n    dependencies: []\n    revisionName: INTERNAL_ADDON|productopsv2|_\n    scopes:\n    - scopeRef:\n        apiVersion: flyadmin.alibaba.com/v1alpha1\n        kind: Namespace\n        name: sreworks\n    - scopeRef:\n        apiVersion: flyadmin.alibaba.com/v1alpha1\n        kind: Stage\n        name: 'prod'\n    - scopeRef:\n        apiVersion: flyadmin.alibaba.com/v1alpha1\n        kind: Cluster\n        name: 'master'\n    parameterValues:\n    - name: TARGET_ENDPOINT\n      value: \"prod-flycore-paas-action\"\n      toFieldPaths:\n        - spec.targetEndpoint\n    - name: STAGE_ID\n      value: 'prod'\n      toFieldPaths:\n        - spec.stageId\n  - dataInputs: []\n    dataOutputs: []\n    dependencies: []\n    revisionName: INTERNAL_ADDON|appmeta|_\n    scopes:\n    - scopeRef:\n        apiVersion: flyadmin.alibaba.com/v1alpha1\n        kind: Namespace\n        name: sreworks\n    - scopeRef:\n        apiVersion: flyadmin.alibaba.com/v1alpha1\n        kind: Stage\n        name: 'prod'\n    - scopeRef:\n        apiVersion: flyadmin.alibaba.com/v1alpha1\n        kind: Cluster\n        name: 'master'\n    parameterValues:\n    - name: STAGE_ID\n      value: 'prod'\n      toFieldPaths:\n        - spec.stageId",
                  docsUrl: '',
                  builtIn: 1,
                  name: '运维桌面',
                  description: '桌面',
                  source: 'swadmin',
                  category: '管理',
                  version: 'v2',
                },
                environments: [
                  {
                    clusterId: 'master',
                    namespaceId: 'sreworks',
                    stageId: 'prod',
                  },
                ],
              },
            ],
          },
        },
        {
          description: '前端分页数据结构',
          data: [
            {
              // "gmtCreate\appId等和table定义的column字段对应"
              gmtCreate: 1630597034000,
              gmtModified: 1630597034000,
              appId: 'desktop',
              options: {
                layout: {
                  type: 'empty',
                },
                logoImg: '/static/icons/desktop.png',
                swapp:
                  "apiVersion: core.oam.dev/v1alpha2\nkind: ApplicationConfiguration\nmetadata:\n  name: deploy-desktop-package\n  annotations:\n    appId: desktop\n    clusterId: master\n    namespaceId: sreworks\n    stageId: prod\nspec:\n  components:\n  - dataInputs: []\n    dataOutputs: []\n    dependencies: []\n    revisionName: INTERNAL_ADDON|productopsv2|_\n    scopes:\n    - scopeRef:\n        apiVersion: flyadmin.alibaba.com/v1alpha1\n        kind: Namespace\n        name: sreworks\n    - scopeRef:\n        apiVersion: flyadmin.alibaba.com/v1alpha1\n        kind: Stage\n        name: 'prod'\n    - scopeRef:\n        apiVersion: flyadmin.alibaba.com/v1alpha1\n        kind: Cluster\n        name: 'master'\n    parameterValues:\n    - name: TARGET_ENDPOINT\n      value: \"prod-flycore-paas-action\"\n      toFieldPaths:\n        - spec.targetEndpoint\n    - name: STAGE_ID\n      value: 'prod'\n      toFieldPaths:\n        - spec.stageId\n  - dataInputs: []\n    dataOutputs: []\n    dependencies: []\n    revisionName: INTERNAL_ADDON|appmeta|_\n    scopes:\n    - scopeRef:\n        apiVersion: flyadmin.alibaba.com/v1alpha1\n        kind: Namespace\n        name: sreworks\n    - scopeRef:\n        apiVersion: flyadmin.alibaba.com/v1alpha1\n        kind: Stage\n        name: 'prod'\n    - scopeRef:\n        apiVersion: flyadmin.alibaba.com/v1alpha1\n        kind: Cluster\n        name: 'master'\n    parameterValues:\n    - name: STAGE_ID\n      value: 'prod'\n      toFieldPaths:\n        - spec.stageId",
                docsUrl: '',
                builtIn: 1,
                name: '运维桌面',
                description: '桌面',
                source: 'swadmin',
                category: '管理',
                version: 'v2',
              },
              environments: [
                {
                  clusterId: 'master',
                  namespaceId: 'sreworks',
                  stageId: 'prod',
                },
              ],
            },
            {
              // "gmtCreate\appId等和table定义的column字段对应"
              gmtCreate: 1630597034000,
              gmtModified: 1630597034000,
              appId: 'desktop',
              options: {
                layout: {
                  type: 'empty',
                },
                logoImg: '/static/icons/desktop.png',
                swapp:
                  "apiVersion: core.oam.dev/v1alpha2\nkind: ApplicationConfiguration\nmetadata:\n  name: deploy-desktop-package\n  annotations:\n    appId: desktop\n    clusterId: master\n    namespaceId: sreworks\n    stageId: prod\nspec:\n  components:\n  - dataInputs: []\n    dataOutputs: []\n    dependencies: []\n    revisionName: INTERNAL_ADDON|productopsv2|_\n    scopes:\n    - scopeRef:\n        apiVersion: flyadmin.alibaba.com/v1alpha1\n        kind: Namespace\n        name: sreworks\n    - scopeRef:\n        apiVersion: flyadmin.alibaba.com/v1alpha1\n        kind: Stage\n        name: 'prod'\n    - scopeRef:\n        apiVersion: flyadmin.alibaba.com/v1alpha1\n        kind: Cluster\n        name: 'master'\n    parameterValues:\n    - name: TARGET_ENDPOINT\n      value: \"prod-flycore-paas-action\"\n      toFieldPaths:\n        - spec.targetEndpoint\n    - name: STAGE_ID\n      value: 'prod'\n      toFieldPaths:\n        - spec.stageId\n  - dataInputs: []\n    dataOutputs: []\n    dependencies: []\n    revisionName: INTERNAL_ADDON|appmeta|_\n    scopes:\n    - scopeRef:\n        apiVersion: flyadmin.alibaba.com/v1alpha1\n        kind: Namespace\n        name: sreworks\n    - scopeRef:\n        apiVersion: flyadmin.alibaba.com/v1alpha1\n        kind: Stage\n        name: 'prod'\n    - scopeRef:\n        apiVersion: flyadmin.alibaba.com/v1alpha1\n        kind: Cluster\n        name: 'master'\n    parameterValues:\n    - name: STAGE_ID\n      value: 'prod'\n      toFieldPaths:\n        - spec.stageId",
                docsUrl: '',
                builtIn: 1,
                name: '运维桌面',
                description: '桌面',
                source: 'swadmin',
                category: '管理',
                version: 'v2',
              },
              environments: [
                {
                  clusterId: 'master',
                  namespaceId: 'sreworks',
                  stageId: 'prod',
                },
              ],
            },
          ],
        },
      ],
    },
  },
  category: 'base',
}
