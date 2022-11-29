import filterForm from './icons/filter-form.svg'
export default {
  id: 'FILTER_FORM',
  type: 'FILTER_FORM',
  name: 'FILTER_FORM',
  title: '过滤表单',
  info: {
    author: {
      name: '',
      url: '',
    },
    description: '过滤表单,过滤项按照定义的几行几列进行排列',
    links: [],
    logos: {
      large: '',
      small: filterForm,
      fontClass: 'FILTER_FORM',
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
      type: 'FILTER_FORM',
      config: {
        title: '过滤表单',
      },
    },
    schema: {},
    dataMock: {},
  },
  category: 'filter',
}
