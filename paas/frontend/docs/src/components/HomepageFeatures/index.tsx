import React from 'react';
import clsx from 'clsx';
import styles from './styles.module.css';

type FeatureItem = {
  title: string;
  Svg: React.ComponentType<React.ComponentProps<'svg'>>;
  description: JSX.Element;
};

const FeatureList: FeatureItem[] = [
  {
    title: '云原生 && 自动化运维',
    Svg: require('@site/static/img/undraw_docusaurus_mountain.svg').default,
    description: (
      <>
        提供通用的云原生应用模板解决方案（兼容OAM），及配合该模板的通用Operator能力，降低企业应用上云门槛，提供统一作业调度平台能力，解决运维大规模集中式调度常见，提升运维效率利器。
      </>
    ),
  },
  {
    title: '运维应用工厂',
    Svg: require('@site/static/img/undraw_docusaurus_tree.svg').default,
    description: (
      <>
        提供运维应用前后端托管的运维应用开发框架及部署平台，前端配置化低代码开发，后端脚手架运维服务插件化集成。
      </>
    ),
  },
  {
    title: '基础 && 数智运维服务',
    Svg: require('@site/static/img/undraw_docusaurus_react.svg').default,
    description: (
      <>
        围绕IT运维工作服务台,“运维数据”的链路管理,基于“感知、决策、执行”的智能运维框架,提供基础运维、数据运维和Auto Pilot的无人值守、AIOps智能运维三个层次的运维体系。
      </>
    ),
  },
];

function Feature({title, Svg, description}: FeatureItem) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center">
        <Svg className={styles.featureSvg} role="img" />
      </div>
      <div className="text--center padding-horiz--md">
        <h3>{title}</h3>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures(): JSX.Element {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
