import filter_tab from './icons/filter_tab.svg'
export default {
  id: 'FILTER_TAB',
  type: 'FILTER_TAB',
  name: 'FILTER_TAB',
  title: 'tab过滤项',
  info: {
    author: {
      name: '',
      url: '',
    },
    description: 'tab型过滤器，切换tab可对目标数据进行分类过滤',
    links: [],
    logos: {
      large: '',
      small: filter_tab,
      fontClass: 'FILTER_TAB',
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
  },
  state: '',
  latestVersion: '1.0',
  configSchema: {
    defaults: {
      type: 'FILTER_TAB',
      config: {
        title: 'TAB过滤项',
      },
    },
    schema: {},
    dataMock: {},
  },
  category: 'filter',
}
