import React from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import Translate, { translate } from '@docusaurus/Translate';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import GitHubButton from 'react-github-btn';
import HomepageFeatures from '@site/src/components/HomepageFeatures';
import useBaseUrl from '@docusaurus/useBaseUrl';
import styles from './index.module.css';
import { useState } from 'react';

function HomepageHeader() {
  const { siteConfig } = useDocusaurusContext();
  return (
    <header className={clsx('hero hero--primary', styles.heroBanner)}>
    </header>
  );
}

export default function Home(): JSX.Element {
  const { siteConfig } = useDocusaurusContext();
  const [showBtn, setShowBtn] = useState(false);
  const switchOff = ()=> setShowBtn(false)
  const switchOn = ()=> setShowBtn(true)

  const [showShadow, setShowShadow] = useState(false);
  const offShadow = ()=> setShowShadow(true)
  // const doClick = ()=> {
  //   if(showBtn) {
  //     switchOff()
  //   } else {
  //     switchOn()
  //   }
  // }
  return (
    <Layout
      title={`Cloud Native DataOps & AIOps Platform`}
      description="SREWorks focuses on an application-centric development model, provides a one-stop cloud-native dataOps&AIOps SaaS management suite.">
      {/* <HomepageHeader /> */}
      <main>
        {/* <HomepageFeatures /> */}
        <div>
          <section className="main-body-one">
            <div className='main-body-one-left'>
              <h1 className="main-body-one-left-title"><Translate>Cloud Native DataOps & AIOps Platform</Translate></h1>
              <div className="main-body-one-left-description">
                <Translate>SREWorks focuses on an application-centric development model, provides a one-stop cloud-native dataOps&AIOps SaaS management suite. It supports the two core capabilities of enterprise: application&resource management and ops development, and helps enterprises quickly achieve the delivery goals of cloud-native applications and resources.</Translate>
              </div>
              <div className="main-body-one-left-button-panel">
                <Link
                  className={clsx(
                    'button'
                  )}
                  style={{ marginRight: 20 }}
                  to={useBaseUrl(`/docs/rr5g10`)}>
                  <Translate>Get Started</Translate>
                </Link>
                <span style={{ "position": "relative", "top": "12px" }}>
                  <GitHubButton
                    href="https://github.com/alibaba/sreworks"
                    data-icon="octicon-star"
                    data-size="large"
                    data-show-count="true"
                    aria-label="Star alibaba/sreworks on GitHub">
                    Star
                  </GitHubButton>
                </span>
              </div>
            </div>
            <div className="main-body-one-right" onClick={offShadow} onMouseEnter={switchOn} onMouseLeave={switchOff}>
              {/* <img alt="homeImage" src={require('@site/static/img/homeImage.png').default}></img> */}
              <video className={showShadow? 'offShadow' : ""} width="100%" preload="auto" src='https://sreworks.oss-cn-beijing.aliyuncs.com/video/introduce2.mp4' controls={showBtn} />
              {/*<video className={showShadow? 'offShadow' : ""} poster={require('@site/static/img/homeImage.png').default} preload="auto" src='https://sreworks.oss-cn-beijing.aliyuncs.com/video/introduce.mp4' controls={showBtn} /> */}
            </div>
          </section>
          <section className="main-body-two">
            <div className="main-body-two-title">
              <h2><Translate>Out-of-the-box operation & management solutions</Translate></h2>
              <h2>&nbsp;</h2>
            </div>
            <div className="main-body-two-description">
              <a className="description-card">
                <h2><Translate>DevOps</Translate></h2>
                <p><Translate>A general cloud application solution (Open Application Model), integrates infrastructure capabilities, and reduces the threshold for enterprises to migrate applications to the cloud.</Translate></p>
                <div className="card-link">
                  <Link
                    to={useBaseUrl('/docs/ii05yo')}>
                    <Translate>Detail</Translate>
                  </Link>
                </div>
              </a>
              <a className="description-card">
                <h2><Translate>DataOps</Translate></h2>
                <p><Translate>A process management focused on the ops data, provided a standardized ops data analysis platform including "data collection, real-time calculation, data service, and data application".</Translate></p>
                <div className="card-link">
                  <Link
                    to={useBaseUrl('/docs/cirgod')}>
                    <Translate>Detail</Translate>
                  </Link>
                </div>
              </a>
              <a className="description-card">
                <h2><Translate>AIOps</Translate></h2>
                <p><Translate>An AIOps framework built on the basis of perception-decision-execution cycle, combined with core aiops algorithm services such as anomaly detection/log clustering/root cause analysis, and supported an "auto pilot" AIOps system.</Translate></p>
                <div className="card-link">
                  <Link
                    to={useBaseUrl('/docs/cirgod#2-智能化运维')}>
                    <Translate>Detail</Translate>
                  </Link>
                </div>
              </a>
              <a className="description-card">
                <h2><Translate>Multi Cloud Cluster Management</Translate></h2>
                <p><Translate>A platform based on k8s cluster, supported management of various cloud vendors or self-built k8s clusters, and cooperated with standard ops warehouses to easily realize multi-cluster management, dataOps and AIOps.</Translate></p>
                <div className="card-link">
                  <Link
                    to={useBaseUrl('/docs/moptgx')}>
                    <Translate>Detail</Translate>
                  </Link>
                </div>
              </a>
              <a className="description-card">
                <h2><Translate>Ops application factory</Translate></h2>
                <p><Translate>A frontend low-code solution & backend scaffolding based on a large number of ops practice cases, helped SRE engineer to quickly produce various ops platforms or tools, and supported the six major ops requirements of "delivery, monitoring, management, control, operation and service".</Translate></p>
                <div className="card-link">
                  <Link
                    to={useBaseUrl('/docs/ap1wm6')}>
                    <Translate>Detail</Translate>
                  </Link>
                </div>
              </a>
              <a className="description-card">
                <h2><Translate>Automation Ops</Translate></h2>
                <p><Translate>A unified job scheduling platform solved large-scale centralized scheduling problems in the unified job scheduling platform, improved ops efficiency.</Translate></p>
                <div className="card-link">
                  <Link
                    to={useBaseUrl('/docs/cirgod#142-数据管理')}>
                    <Translate>Detail</Translate>
                  </Link>
                </div>
              </a>
            </div>
          </section>
          <section className="main-body-three">
            <div className="main-body-three-left">
              <h2 className="main-body-three-left-title"><Translate>DataOps & AIOps System</Translate></h2>
              <div className="main-body-three-left-description">
                <Translate>The dataops & aiops system incubated in Alibaba's big data scenarios can easily support hyperscale clusters, ranging from application ops to multi-cloud management solutions, from dataops warehouses to aiops algorithms. All services are readily available in SREWorks.</Translate>
              </div>
              <div className="main-body-three-left-button-panel">
                <Link
                  className={clsx(
                    'button button--primary'
                  )}
                  style={{ marginRight: 20 }}
                  to={useBaseUrl('/docs/cirgod')}>
                  <Translate>Learn More</Translate>
                </Link>
              </div>
            </div>
            <div>
              <img style={{ height: 300, marginTop: 40, marginLeft: 100 }} src="https://sreworks.oss-cn-beijing.aliyuncs.com/svg/dataops-aiops.svg" />
            </div>
          </section>
          <section className="main-body-three">
            <div>
              <img style={{ height: 300, marginTop: 40 }} src="https://sreworks.oss-cn-beijing.aliyuncs.com/svg/k8s-devops.svg" />
            </div>
            <div className="main-body-three-left">
              <h2 className="main-body-three-left-title"><Translate>Cloud native construction and delivery system</Translate></h2>
              <div className="main-body-three-left-description">
                <Translate>The closed-loop application construction and delivery process under cloud native eliminates the trouble of maintaining complex construction and deploy pipline, and can expand construction performance at any time. Build your application once and run it anywhere in multi-cloud environment.</Translate>
              </div>
              <div className="main-body-three-left-button-panel">
                <Link
                  className={clsx(
                    'button button--primary'
                  )}
                  style={{ marginRight: 20 }}
                  to={useBaseUrl('/docs/ap1wm6')}>
                  <Translate>Learn More</Translate>
                </Link>
              </div>
            </div>
          </section>
          <section className="main-body-three">
            <div className="main-body-three-left">
              <h2 className="main-body-three-left-title"><Translate>Frontend market with tons of widgets</Translate></h2>
              <div className="main-body-three-left-description">
                <Translate>The low-code frontend IDE provides both agility and full business function support. Rich, flexible and practical frontend widget market, open custom widget integration capabilities, to meet the needs of users in diverse personalized scenarios.</Translate>
              </div>
              <div className="main-body-three-left-button-panel">
                <Link
                  className={clsx(
                    'button button--primary'
                  )}
                  style={{ marginRight: 20 }}
                  to={useBaseUrl('/docs/ou9k9g')}>
                  <Translate>Learn More</Translate>
                </Link>
              </div>
            </div>
            <div>
              <img style={{ height: 500, marginTop: -50 }} src="https://sreworks.oss-cn-beijing.aliyuncs.com/svg/lowcode-active2.svg" />
            </div>
          </section>
        </div>
      </main>
    </Layout>
  );
}
