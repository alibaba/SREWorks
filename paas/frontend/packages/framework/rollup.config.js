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
      assets: ['src/assets'],
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
    replace({
      'process.env.NODE_ENV': JSON.stringify(process.env.NODE_ENV),
    }),
    terser(),
  ],
  external: ['react', 'react-dom'],
})

const mainConfig = {
  input: './src/index.js',
  output: [
    {
      file: './dist/index-umd.js',
      format: 'umd',
      name: 'sre_framework',
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
  sourcemap: false,
  ...configFn('index'),
}
mainConfig.plugins.unshift(del({ targets: ['lib/*', 'dist/*'] }))

export default [mainConfig]
