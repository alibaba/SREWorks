// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const lightCodeTheme = require('prism-react-renderer/themes/github');
const darkCodeTheme = require('prism-react-renderer/themes/dracula');
//const versionConfig = {
//  latestVersion: "v1.2"
//}

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'SREWorks',
  tagline: 'SREWorks 专注于以应用为中心的一站式“云原生”、“数智化”运维SaaS管理套件。提供企业的应用&资源管理及运维开发两大核心能力，帮助企业实现云原生应用&资源的交付运维',
  url: "https://sreworks.opensource.alibaba.com/",
  baseUrl: '/docs/',
  onBrokenMarkdownLinks: 'warn',
  favicon: 'img/favicon.ico',
  onBrokenLinks: "ignore",
  // GitHub pages deployment config.
  // If you aren't using GitHub pages, you don't need these.
  organizationName: 'alibaba', // Usually your GitHub org/user name.
  projectName: 'SREWorks', // Usually your repo name.
  // Even if you don't use internalization, you can use this field to set useful
  // metadata like html lang. For example, if your site is Chinese, you may want
  // to replace "en" with "zh-Hans".

  i18n: {
    defaultLocale: 'en',
    locales: ['en', 'zh'],
    localeConfigs: {
      en: {
        label: 'English',
      },
      zh: {
        label: '简体中文',
      },
    },
  },

  presets: [
    [
      'classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          sidebarPath: require.resolve('./sidebars.json'),
          editUrl: function(params){
             let docName = params.permalink.split("/")[params.permalink.split("/").length - 1].split(".")[0];
             return "https://www.yuque.com/sreworks-doc/docs/" + docName;
          },
        },
        theme: {
          customCss: require.resolve('./src/css/sreworks.scss'),
        },
      }),
    ],
  ],
  plugins: [
    'docusaurus-plugin-sass',
  ],

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      navbar: {
        title: 'SREWorks',
        hideOnScroll: true,
        logo: {
          alt: 'sreworks Logo',
          src: 'img/sreworks.png',
          href: '/',
          target: '_self',
        },

        items: [
          {
            label: "docs",
            position: 'right',
            to: 'docs'
          },
          {
            type: 'localeDropdown',
            position: 'right',
          },
        ],
      },
      metadata: [
        {
          name: 'keywords',
          content: 'sreworks 官方,sreworks官方文档,SREWorks,SREWorks官网,SRE works,SREworks,srework,srewoks,sreworks,srework,sre文档,sre-works,sre official,sre_works,云原生运维,大数据运维,阿里运维,云原生k8s,智能运维,数智运维,简单运维,高效运维,appmaniger,一站式运维,saas运维'
        },
        {
          name: 'description',
          content: 'SREWorks 专注于以应用为中心的一站式“云原生”、“数智化”运维SaaS管理套件。提供企业的应用&资源管理及运维开发两大核心能力，帮助企业实现云原生应用&资源的交付运维'
        },
      ],
      announcementBar: {
        id: 'announcementBar-2',
        content: '⭐ 开源不易，如果觉得本项目对您的工作还是有帮助的话， 请帮忙在<a target="_blank" rel="noopener noreferrer" href="https://github.com/alibaba/sreworks">GitHub</a> 点个⭐️',
      },
      footer: {
        style: 'dark',
        links: [
          {
             title: '文档',
             items: [
               {
                  label: '快速安装',
                  href: '/docs/rr5g10'
               },{

                  label: '核心思想',
                  href: '/docs/eg91va',
               },{
                  label: '数智运维',
                  href: '/docs/cirgod',
              }
             ]
          },
          {
            title: '社区',
            items: [
              {
                label: 'Github',
                href: 'https://github.com/alibaba/sreworks/issues',
              },{
                html: '<div class="wechat"> <span class="wechat-label">钉钉交流群</span> <a class="wechat-img" rel="noreferrer noopener" aria-label="DingTalk Group"><img src="https://sreworks.oss-cn-beijing.aliyuncs.com/logo/ding.jpg"></div>'
              },{
                html: '<div class="wechat"> <span class="wechat-label">微信交流群</span> <a class="wechat-img" rel="noreferrer noopener" aria-label="Wechat Group"><img src="https://sreworks.oss-cn-beijing.aliyuncs.com/logo/weixin-helper.jpg" alt="Broker wechat to add you into the user group."></div>'
              }
            ],
          }
        ],
        copyright: `Copyright © ${new Date().getFullYear()} SREWorks | 浙公网安备: 33010002000092号 | ICP备案网站信息: 浙B2-20120091-4`,
      },
      prism: {
        theme: lightCodeTheme,
        darkTheme: darkCodeTheme,
      },
      colorMode: {
        defaultMode: 'light',
        disableSwitch: true,
        respectPrefersColorScheme: false,
      },
    }),
};
module.exports = config;
