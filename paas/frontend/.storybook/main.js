module.exports = {
  "stories": [
    "../packages/components/src/**/*.stories.mdx",
    "../packages/components/src/**/*.stories.@(js|jsx|ts|tsx)",
    "../packages/widgets/src/**/*.stories.mdx",
    "../packages/widgets/src/**/*.stories.@(js|jsx|ts|tsx)"
  ],
  "addons": [
    "@storybook/addon-links",
    "@storybook/addon-essentials",
    "@storybook/addon-interactions",
  ],
  "framework": "@storybook/react",
  "core": {
    "builder": "@storybook/builder-webpack5"
  },
  webpackFinal: async (config, { configType }) => {
    // `configType` has a value of 'DEVELOPMENT' or 'PRODUCTION'
    // You can change the configuration based on that.
    // 'PRODUCTION' is used when building the static version of storybook.

    // Make whatever fine-grained changes you need
    config.module.rules = [
      ...config.module.rules.map(rule => {
        if (/svg/.test(rule.test)) {
          // Silence the Storybook loaders for SVG files
          return { ...rule, exclude: /\.svg$/i }
        }
        return rule
      }),
      // Add your custom SVG loader
      {
        test: /\.svg$/i,
        use: ['@svgr/webpack']
      },
      {
        test: /\.less$/i,
        use: [
          'style-loader',
          {
            loader: 'css-loader',
          },
          {
            loader: 'less-loader',
            options: {
              lessOptions: {
                // strictMath: true,
                javascriptEnabled: true,
              },
            },
          },
        ],
      },
    ]

    // Return the altered config
    return config;
  },
}
