import step_form from './icons/step_form.svg'
export default {
  id: 'STEP_FORM',
  type: 'STEP_FORM',
  name: 'STEP_FORM',
  title: '步骤表单',
  info: {
    author: {
      name: '',
      url: '',
    },
    description: '步骤表单,用于组成分步操作,分步操作的非最后一步',
    links: [],
    logos: {
      large: '',
      small: step_form,
      fontClass: 'STEP_FORM',
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
      type: 'STEP_FORM',
      config: {
        title: '步骤表单',
      },
    },
    schema: {},
    dataMock: {},
  },
  category: 'action',
}
