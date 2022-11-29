import icon from './icon.svg'
export default {
  id: 'Grafana',
  type: 'Grafana',
  name: 'Grafana',
  title: 'Grafana',
  info: {
    author: {
      name: '',
      url: '',
    },
    description: '',
    links: [],
    logos: {
      large: '',
      small: icon,
      fontClass: 'Grafana',
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
    docs: '',
  },
  state: '',
  latestVersion: '1.0',
  configSchema: {
    defaults: {
      type: 'Alert',
      config: {
        url: '',
      },
    },
    schema: {
      type: 'object',
      properties: {
        url: {
          description: 'Grafana链接地址,该组件提供一个iframe容器，需将Grafana链接地址配置到此处',
          title: 'Grafana链接地址',
          required: true,
          'x-component': 'Text',
          type: 'string',
        },
      },
    },
    dataMock: {},
  },
  category: 'base',
}
