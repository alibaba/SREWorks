module.exports = function (api) {
  api.cache(true)
  return {
    babelrcRoots: [
      'app/*',
      'packages/*', // 将子程序包都作为工作目录
    ],
  }
}
