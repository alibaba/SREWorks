import step_action from './icons/step_action.svg'
export default {
  id: 'STEP_ACTION',
  type: 'STEP_ACTION',
  name: 'STEP_ACTION',
  title: '分步操作',
  info: {
    author: {
      name: '',
      url: '',
    },
    description: '分步操作,一般用于比较复杂的业务场景,需要拆分成多个表单步骤进行实现',
    links: [],
    logos: {
      large: '',
      small: step_action,
      fontClass: 'STEP_ACTION',
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
      type: 'STEP_ACTION',
      config: {
        title: '分步操作',
      },
    },
    schema: {},
    dataMock: {},
  },
  category: 'action',
}
