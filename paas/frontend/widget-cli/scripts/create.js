process.on('unhandledRejection', err => {
  throw err;
});
const fs = require('fs-extra');
const path = require('path');
const chalk = require('chalk');
const os = require('os');
const execSync = require('child_process').execSync;

const gitignore = `# dependencies
/node_modules
/coverage
/build
/dist
.DS_Store
npm-debug.log*
debug.log*
yarn-debug.log*
yarn-error.log*
/.idea
`;
const isInGitRepository = () => {
  try {
    execSync('git rev-parse --is-inside-work-tree', { stdio: 'ignore' });
    return true;
  } catch (e) {
    return false;
  }
}
const tryGitInit = (appPath) => {
  let didInit = false;
  try {
    execSync('git --version', { stdio: 'ignore', cwd: appPath });
    if (isInGitRepository(appPath)) {
      return false;
    }

    execSync('git init', { stdio: 'ignore', cwd: appPath });
    didInit = true;

    execSync('git add -A', { stdio: 'ignore', cwd: appPath });
    execSync('git commit -m "Initial commit from Build Tool"', {
      stdio: 'ignore',
      cwd: appPath,
    });
    return true;
  } catch (e) {
    if (didInit) {
      try {
        // unlinkSync() doesn't work on directories.
        fs.removeSync(path.join(appPath, '.git'));
      } catch (removeErr) {
        // Ignore.
      }
    }
    return false;
  }
}
const create = (appName, appPath) => {
  const ownPath = path.dirname(require.resolve(path.join(__dirname, '..', 'package.json')));
  const appPackage = {};
  appPackage.name = appName;
  appPackage.version = '0.0.1';
  appPackage.privete = true;
  appPackage.dependencies = {
    "dva": "2.4.1",
    "dva-loading": "^1.0.4",
    "@ant-design/compatible": "^1.1.2",
    "@ant-design/icons": "^4.7.0",
    "@ant-design/pro-card": "^2.0.10",
    "antd": "4.17.4",
    "axios": "^0.27.2",
    "bizcharts": "4.1.15",
    "brace": "^0.11.1",
    "classnames": "^2.3.2",
    "copy-to-clipboard": "3.3.1",
    "eventemitter3": "^4.0.7",
    "events": "^3.3.0",
    "history": "^5.3.0",
    "html2canvas": "^1.4.1",
    "i18next": "^10.6.0",
    "intl-messageformat": "2.2.0",
    "jquery": "^3.6.1",
    "js-yaml": "^4.1.0",
    "jsonexport": "^3.2.0",
    "localforage": "^1.10.0",
    "lodash": "^4.17.21",
    "moment": "2.27.0",
    "moment-duration-format": "2.3.2",
    "mustache": "4.2.0",
    "nprogress": "^0.2.0",
    "promise": "8.1.0",
    "prop-types": "^15.8.1",
    "qs": "^6.11.0",
    "rc-color-picker": "^1.2.6",
    "rc-queue-anim": "^1.2.2",
    "rc-tween-one": "1.7.3",
    "react": "16.14.0",
    "react-ace": "^10.1.0",
    "react-color": "^2.19.3",
    "react-dnd": "^16.0.0",
    "react-dnd-html5-backend": "^16.0.0",
    "react-dom": "16.14.0",
    "react-grid-layout": "^1.3.4",
    "react-highlight": "^0.13.0",
    "react-infinite-scroller": "^1.2.6",
    "react-jsx-parser": "^1.29.0",
    "react-markdown": "^4.3.1",
    "react-onclickoutside": "^6.12.2",
    "react-redux": "^8.0.4",
    "react-router": "4.3.1",
    "react-router-dom": "^6.4.2",
    "react-sizeme": "^3.0.2",
    "react-sortablejs": "6.0.0",
    "shortid": "^2.2.16",
    "sortablejs": "1.13.0",
    "uuid": "^9.0.0",
  };

  appPackage.devDependencies = {
    "copy-webpack-plugin": "^11.0.0",
    "antd-theme-generator": "1.1.6",
    "autoprefixer": "^10.4.8",
    "@babel/plugin-proposal-decorators": "^7.20.0",
    "@babel/cli": "7.18.10",
    "@babel/core": "7.18.13",
    "@babel/plugin-transform-runtime": "7.19.6",
    "@babel/preset-env": "7.18.10",
    "@babel/preset-react": "7.18.6",
    "antd-theme-generator": "1.1.6",
    "babel-loader": "8.2.5",
    "babel-preset-react-app": "9.1.2",
    "compression-webpack-plugin": "^8.0.1",
    "copy-webpack-plugin": "^11.0.0",
    "cross-env": "^7.0.3",
    "css-loader": "^6.7.1",
    "css-minimizer-webpack-plugin": "^4.2.2",
    "eslint-config-react-app": "^7.0.1",
    "eslint-webpack-plugin": "^3.2.0",
    "file-loader": "^6.2.0",
    "fs-extra": "7.0.0",
    "html-webpack-plugin": "^5.5.0",
    "less-loader": "^11.1.0",
    "less-vars-to-js": "1.3.0",
    "mini-css-extract-plugin": "^2.6.1",
    "node-polyfill-webpack-plugin": "^2.0.1",
    "postcss-loader": "^7.0.1",
    "postcss-preset-env": "^7.8.2",
    "prettier": "2.7.1",
    "process": "^0.11.10",
    "sass-loader": "^13.1.0",
    "style-loader": "^3.3.1",
    "stylus-loader": "^7.1.0",
    "thread-loader": "^3.0.4",
    "url-loader": "^4.1.1",
    "webpack": "5.74.0",
    "webpack-bundle-analyzer": "^4.7.0",
    "webpack-cli": "4.10.0",
    "webpack-dev-server": "^4.11.1",
    "webpack-merge": "5.8.0",
    "eslint": "8.22.0",
    "eslint-config-prettier": "8.5.0",
    "eslint-plugin-prettier": "4.2.1",
    "@babel/eslint-parser": "7.18.9"
  };
  // Setup the script rules
  appPackage.scripts = {
    serve: 'cross-env NODE_ENV=development webpack serve -c config/webpack.config.js',
    umd: "npm run lint && cross-env NODE_ENV=production webpack -c config/webpack.config.umd.js",
    lint: "prettier --write \"**/src/**/*.js?(x)\""
  };
  appPackage.browserslist = ['>0.2%', 'not dead', 'not ie <= 11', 'not op_mini all'];
  appPackage.homePage = "./"
  appPackage.resolutions = {
    "react": "16.14.0",
    "react-dom": "16.14.0"
  }
  fs.writeFileSync(path.join(appPath, 'package.json'), JSON.stringify(appPackage, null, 2) + os.EOL);

  fs.copySync(path.join(ownPath, 'template'), appPath);
  // fs.copySync(path.join(ownPath, 'config'), path.join(appPath, 'config'));
  // fs.copySync(path.join(ownPath, 'scripts'), path.join(appPath, 'scripts'));
  fs.writeFileSync(path.join(appPath, '.gitignore'), gitignore);
  if (tryGitInit(appPath)) {
    console.log(chalk.green("create success !"));
  }
}
const args = process.argv.slice(2);
const rootDir = process.cwd();
const projectName = args[0];

if (typeof projectName === 'undefined') {
  console.error('Please specify the project directory.');
  process.exit(1);
}

const projectPath = path.join(rootDir, projectName);

fs.ensureDirSync(projectPath);

create(projectName, path.join(rootDir, projectName));

