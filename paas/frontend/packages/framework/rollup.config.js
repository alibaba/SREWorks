// import resolve from '@rollup/plugin-node-resolve'
// import commonjs from '@rollup/plugin-commonjs'
// import babel from '@rollup/plugin-babel'
// import { terser } from 'rollup-plugin-terser'
// import json from '@rollup/plugin-json'
// import * as path from 'path'
// import postcss from 'rollup-plugin-postcss'
// import image from '@rollup/plugin-image';

// export default {
//   input: 'src/index.js',
//   output: {
//     dir: path.dirname('dist/bundle.js'),
//     format: 'es',
//     preserveModules: true, // 保留模块结构
//     preserveModulesRoot: 'src',
//   },
//   external: ['react', 'react-dom', 'antd'],
//   plugins: [
//     babel({
//       exclude: 'node_modules/**', // 防止打包node_modules下的文件
//     }),
//     resolve(),
//     commonjs(),
//     json(),
//     postcss({
//       use: [
//         [
//           'less',
//           {
//             javascriptEnabled: true,
//           },
//         ],
//       ],
//     }),
//     image(),
//   ],
// }

import babel from 'rollup-plugin-babel'
import commonjs from 'rollup-plugin-commonjs'
import postcss from 'rollup-plugin-postcss'
import autoprefixer from 'autoprefixer'
import cssnano from 'cssnano'
import { terser } from 'rollup-plugin-terser'
import del from 'rollup-plugin-delete'
import json from '@rollup/plugin-json'
import image from '@rollup/plugin-image'
import replace from '@rollup/plugin-replace'
import copy from 'rollup-plugin-copy-assets'
import requireContext from 'rollup-plugin-require-context'

const configFn = (name) => ({
  plugins: [
    babel({
      exclude: 'node_modules/**',
      runtimeHelpers: true,
    }),
    commonjs({ exclude: 'node_modules/**' }),
    requireContext(),
    json(),
    image(),
    copy({
      assets: [
        // You can include directories
        'src/assets',
      ],
    }),
    postcss({
      use: [
        [
          'less',
          {
            javascriptEnabled: true,
          },
        ],
        ['sass'],
      ],
      plugins: [autoprefixer(), cssnano()],
      extract: `theme/${name}.css`,
    }),
    // terser(),
    replace({
      'process.env.NODE_ENV': JSON.stringify(process.env.NODE_ENV),
    }),
  ],
  external: ['react', 'react-dom'],
})

const umdConfig = {
  input: './src/index.js',
  output: [
    {
      file: './dist/index-umd.js',
      format: 'umd',
      name: 'myLib',
    },
    {
      file: './dist/index-es.js',
      format: 'es',
    },
    {
      file: './dist/index-cjs.js',
      format: 'cjs',
    },
  ],
  sourcemap: true,
  ...configFn('index'),
}
umdConfig.plugins.unshift(del({ targets: ['lib/*', 'dist/*'] }))

export default [umdConfig]
