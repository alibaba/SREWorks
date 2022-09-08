import React from 'react'

import { Loading } from '.'

// More on default export: https://storybook.js.org/docs/react/writing-stories/introduction#default-export
export default {
  title: 'Components/Loading',
  component: Loading,
  // More on argTypes: https://storybook.js.org/docs/react/api/argtypes
  // argTypes: {
  //   backgroundColor: { control: 'color' },
  // },
}

// More on component templates: https://storybook.js.org/docs/react/writing-stories/introduction#using-args
const Template = (args) => <Loading {...args} />

export const LoadingComp = Template.bind({})
// More on args: https://storybook.js.org/docs/react/writing-stories/args
LoadingComp.args = {
  platformName: 'Loading',
}
