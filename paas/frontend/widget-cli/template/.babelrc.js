module.exports = {
    presets: ['react-app'],
    plugins: [
      [
        '@babel/plugin-proposal-decorators',
        {
          legacy: true,
        },
      ],
      [
        '@babel/plugin-transform-runtime',
        {
          helpers: true,
        },
      ],
    ],
  }
  