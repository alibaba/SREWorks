import filterMix from './icons/filter-mix.svg'
export default {
  id: 'FILTER_MIX',
  type: 'FILTER_MIX',
  name: 'FILTER_MIX',
  title: '高级过滤条',
  info: {
    author: {
      name: '',
      url: '',
    },
    description: '高级过滤条,显示为一个过滤项,过滤项可以进行切换、追加、替换等进行组合过滤',
    links: [],
    logos: {
      large: '',
      small: filterMix,
      fontClass: 'FILTER_MIX',
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
      type: 'FILTER_MIX',
      config: {
        title: '高级过滤条',
      },
    },
    schema: {},
    dataMock: {},
  },
  category: 'filter',
}
