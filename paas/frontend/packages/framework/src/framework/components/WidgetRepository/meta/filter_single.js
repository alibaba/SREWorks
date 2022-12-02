import filterSingle from './icons/filter-single.svg'
export default {
  id: 'FILTER_SINGLE',
  type: 'FILTER_SINGLE',
  name: 'FILTER_SINGLE',
  title: '单过滤项',
  info: {
    author: {
      name: '',
      url: '',
    },
    description: '单过滤项,每次只能进行一个过滤项过滤,可进行切换过滤项',
    links: [],
    logos: {
      large: '',
      small: filterSingle,
      fontClass: 'FILTER_SINGLE',
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
      type: 'FILTER_SINGLE',
      config: {
        title: '单过滤项',
      },
    },
    schema: {},
    dataMock: {},
  },
  category: 'filter',
}
