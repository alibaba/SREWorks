import React, { Component } from 'react'
import ScreenHeader from './ScreenHeader'
import ChartFactory from '../ChartFactory'
import styles from './index.less'

export default class ScreenOne extends Component {
  render() {
    const { widgetConfig = {}, widgetData, nodeParams = {} } = this.props
    let { backgroundImg, screenTitle, chartDisplayConfig } = widgetConfig
    let { screenDisplayConfig = {} } = this.props

    return (
      <section
        id="datav-content"
        style={{
          background: backgroundImg
            ? `#05080f url(${backgroundImg}) no-repeat center`
            : `#05080f url(https://datav.oss-cn-hangzhou.aliyuncs.com/uploads/images/6c14af36fd6c17b197b9013c113b0079.png) no-repeat center`,
          backgroundPosition: 'center bottom',
          backgroundRepeat: 'no-repeat',
          backgroundColor: 'rgb(51, 51, 51)',
        }}
      >
        <ScreenHeader title={screenTitle || 'SREWorks大屏'} />
        <div className={styles.screenTwoBodyTop}>
          <section className={styles.topBorderImg}>
            {screenDisplayConfig['chart_one'] ? (
              screenDisplayConfig['chart_one'].map((item) => {
                return ChartFactory.createScreenChart(item)
              })
            ) : (
              <div className={styles.noChartData}>图表1</div>
            )}
          </section>
          <section className={styles.topBorderImg}>
            {screenDisplayConfig['chart_two'] ? (
              screenDisplayConfig['chart_two'].map((item) => {
                return ChartFactory.createScreenChart(item)
              })
            ) : (
              <div className={styles.noChartData}>图表2</div>
            )}
          </section>
          <section className={styles.topBorderImg}>
            {screenDisplayConfig['chart_three'] ? (
              screenDisplayConfig['chart_three'].map((item) => {
                return ChartFactory.createScreenChart(item)
              })
            ) : (
              <div className={styles.noChartData}>图表3</div>
            )}
          </section>
        </div>
        <div className={styles.screenTwoBodyMiddle}>
          <section className={styles.middleBorderImg}>
            {screenDisplayConfig['chart_four'] ? (
              screenDisplayConfig['chart_four'].map((item) => {
                return ChartFactory.createScreenChart(item)
              })
            ) : (
              <div className={styles.noChartData}>图表4</div>
            )}
          </section>
          <section className={styles.middleBorderImg}>
            {screenDisplayConfig['chart_five'] ? (
              screenDisplayConfig['chart_five'].map((item) => {
                return ChartFactory.createScreenChart(item)
              })
            ) : (
              <div className={styles.noChartData}>图表5</div>
            )}
          </section>
          <section className={styles.middleBorderImg}>
            {screenDisplayConfig['chart_six'] ? (
              screenDisplayConfig['chart_six'].map((item) => {
                return ChartFactory.createScreenChart(item)
              })
            ) : (
              <div className={styles.noChartData}>图表6</div>
            )}
          </section>
        </div>
        <div className={styles.scrennTwoBodyBottom}>
          <section className={styles.bottomBorderImg}>
            {screenDisplayConfig['chart_seven'] ? (
              screenDisplayConfig['chart_seven'].map((item) => {
                return ChartFactory.createScreenChart(item)
              })
            ) : (
              <div className={styles.noChartData}>图表7</div>
            )}
          </section>
          <section className={styles.bottomBorderImg}>
            {screenDisplayConfig['chart_eight'] ? (
              screenDisplayConfig['chart_eight'].map((item) => {
                return ChartFactory.createScreenChart(item)
              })
            ) : (
              <div className={styles.noChartData}>图表8</div>
            )}
          </section>
          <section className={styles.bottomBorderImg}>
            {screenDisplayConfig['chart_nine'] ? (
              screenDisplayConfig['chart_nine'].map((item) => {
                return ChartFactory.createScreenChart(item)
              })
            ) : (
              <div className={styles.noChartData}>图表9</div>
            )}
          </section>
        </div>
        <div className={styles.screenTwoBodyBottom}>
          <section className={styles.bottomBorderImg}>
            {screenDisplayConfig['chart_ten'] ? (
              screenDisplayConfig['chart_ten'].map((item) => {
                return ChartFactory.createScreenChart(item)
              })
            ) : (
              <div className={styles.noChartData}>图表10</div>
            )}
          </section>
          <section className={styles.bottomBorderImg}>
            {screenDisplayConfig['chart_eleven'] ? (
              screenDisplayConfig['chart_eleven'].map((item) => {
                return ChartFactory.createScreenChart(item)
              })
            ) : (
              <div className={styles.noChartData}>图表11</div>
            )}
          </section>
          <section className={styles.bottomBorderImg}>
            {screenDisplayConfig['chart_twelve'] ? (
              screenDisplayConfig['chart_twelve'].map((item) => {
                return ChartFactory.createScreenChart(item)
              })
            ) : (
              <div className={styles.noChartData}>图表12</div>
            )}
          </section>
        </div>
      </section>
    )
  }
}
