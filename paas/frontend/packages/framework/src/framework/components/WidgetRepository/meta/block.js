import block from './icons/block.svg'
export default {
  id: 'BLOCK',
  type: 'BLOCK',
  name: 'BLOCK',
  title: '页面区块',
  info: {
    author: {
      name: '',
      url: '',
    },
    description: '页面定义区块',
    links: [],
    logos: {
      large: '',
      small: block,
      fontClass: 'BLOCK',
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
      type: 'BLOCK',
      config: {
        title: '区块',
      },
    },
    schema: {},
    dataMock: {},
  },
  category: 'block',
}
