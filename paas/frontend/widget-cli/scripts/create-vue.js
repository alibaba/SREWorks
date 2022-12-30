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
      "core-js": "^3.6.5",
      "element-ui": "^2.15.9",
      "vue": "^2.6.11"
    };
  
    appPackage.devDependencies = {
      "@vue/cli-plugin-babel": "~4.4.0",
      "@vue/cli-plugin-eslint": "~4.4.0",
      "@vue/cli-service": "~4.4.0",
      "babel-eslint": "^10.1.0",
      "eslint": "^6.7.2",
      "eslint-plugin-vue": "^6.2.2",
      "vue-template-compiler": "^2.6.11"
    };
    // Setup the script rules
    appPackage.scripts = {
      "serve": "vue-cli-service serve",
      "build": "vue-cli-service build",
      "lint": "vue-cli-service lint",
      "umd": "vue-cli-service build --target lib --name vuetimeline src/umd.js"
    }
    appPackage.browserslist = ['>0.2%', 'not dead', 'not ie <= 11', 'not op_mini all'];
    appPackage.eslintConfig = {
      "root": true,
      "env": {
        "node": true
      },
      "extends": [
        "plugin:vue/essential",
        "eslint:recommended"
      ],
      "parserOptions": {
        "parser": "babel-eslint"
      },
      "rules": {}
    }
    
    fs.writeFileSync(path.join(appPath, 'package.json'), JSON.stringify(appPackage, null, 2) + os.EOL);
  
    fs.copySync(path.join(ownPath, 'vue-template'), appPath);
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
  