module.exports = {
  presets: [
    [
      '@babel/preset-env',
      {
        useBuiltIns: 'entry',
        corejs: '3.22',
      },
    ],
    ["@babel/preset-react"]
  ],
  plugins:[
    [
      "@babel/plugin-proposal-decorators",
      {
        "legacy": true
      }
    ],
    '@babel/plugin-transform-runtime'
  ]
}
