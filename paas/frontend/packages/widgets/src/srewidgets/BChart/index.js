/*
 * @version: 2.0.0
 * @Author: deeham.ww
 * @Date: 2022-09-21 15:37:54
 * @LastEditors: deeham.ww
 * @LastEditTime: 2022-11-22 14:00:32
 */
import React, { Component } from 'react'
import { LineChart, ColumnChart, BarChart, ScatterChart } from 'bizcharts'
import { safeEval, ChartTool } from '@sreworks/shared-utils'
import _ from 'lodash'

class BChart extends Component {
  constructor(props) {
    super(props)
    this.state = {
      chartData: null,
      nodeParams: _.cloneDeep(props.nodeParams),
    }
    this.timerInterval = null
  }
  componentDidMount() {
    let { widgetConfig = {} } = this.props
    let { period } = widgetConfig
    if (period) {
      this.intervalLoad()
      this.timerInterval = setInterval(() => {
        this.intervalLoad()
      }, Number(period) || 10000)
    }
  }
  async intervalLoad() {
    let { widgetConfig = {} } = this.props
    let allProps = { ...this.props }
    let data = await ChartTool.loadChartData(allProps, widgetConfig)
    this.setState({
      chartData: data,
    })
  }
  componentWillUnmount() {
    if (this.timerInterval) {
      clearInterval(this.timerInterval)
    }
  }
  render() {
    const { widgetConfig = {}, widgetData } = this.props
    let { chartData } = this.state
    let {
      theme,
      appendPadding,
      height,
      width,
      colorType,
      chartPosition,
      chartType = 'Line',
      xField,
      yField,
      seriesField,
      chartTitle,
      legendPosition,
      isSlider,
      isLegend,
      period,
      advancedConfig = '',
    } = widgetConfig
    if (appendPadding && appendPadding.indexOf(',') > -1) {
      appendPadding = appendPadding.split(',').map((item) => Number(item))
    }
    let data = [
      {
        year: '1991',
        value: 3,
      },
      {
        year: '1992',
        value: 4,
      },
      {
        year: '1993',
        value: 3.5,
      },
      {
        year: '1994',
        value: 5,
      },
      {
        year: '1995',
        value: 4.9,
      },
      {
        year: '1996',
        value: 6,
      },
      {
        year: '1997',
        value: 7,
      },
      {
        year: '1998',
        value: 9,
      },
      {
        year: '1999',
        value: 13,
      },
    ]
    let finalData = chartData || widgetData || data
    let advConf = {}
    if (advancedConfig && advancedConfig.length > 40) {
      advConf = safeEval('(' + advancedConfig + ')(widgetData)', { widgetData: finalData })
    }
    if (chartType && chartType === 'Interval') {
      return (
        <ColumnChart
          autoFit
          width={width && Number(width)}
          height={height && Number(height)}
          theme={theme || 'light'}
          padding={appendPadding || 'auto'}
          data={finalData}
          title={{
            visible: !!chartTitle,
            text: chartTitle || '',
          }}
          xField={xField || 'year'}
          yField={yField || 'value'}
          seriesField={seriesField}
          color={(colorType && colorType['color']) || ''}
          legend={{
            visible: isLegend,
            position: legendPosition,
          }}
          interactions={
            isSlider
              ? [
                  {
                    type: 'slider',
                    cfg: {
                      start: 0,
                      end: 1,
                    },
                  },
                ]
              : []
          }
          {...advConf}
        />
      )
    }
    if (chartType && chartType === 'transposeInterval') {
      return (
        <BarChart
          autoFit
          width={width && Number(width)}
          height={height && Number(height)}
          theme={theme || 'light'}
          padding={appendPadding || 'auto'}
          data={finalData}
          title={{
            visible: !!chartTitle,
            text: chartTitle || '',
          }}
          xField={xField || 'year'}
          yField={yField || 'value'}
          seriesField={seriesField}
          color={(colorType && colorType['color']) || ''}
          legend={{
            visible: isLegend,
            position: legendPosition,
          }}
          interactions={
            isSlider
              ? [
                  {
                    type: 'slider',
                    cfg: {
                      start: 0,
                      end: 1,
                    },
                  },
                ]
              : []
          }
          {...advConf}
        />
      )
    }
    if (chartType && chartType === 'Point') {
      return (
        <ScatterChart
          autoFit
          width={width && Number(width)}
          height={height && Number(height)}
          theme={theme || 'light'}
          padding={appendPadding || 'auto'}
          data={finalData}
          title={{
            visible: !!chartTitle,
            text: chartTitle || '',
          }}
          xField={xField || 'year'}
          yField={yField || 'value'}
          seriesField={seriesField}
          color={(colorType && colorType['color']) || ''}
          legend={{
            visible: isLegend,
            position: legendPosition,
          }}
          interactions={
            isSlider
              ? [
                  {
                    type: 'slider',
                    cfg: {
                      start: 0,
                      end: 1,
                    },
                  },
                ]
              : []
          }
          //  {...advConf}
        />
      )
    }
    return (
      <LineChart
        autoFit
        width={width && Number(width)}
        height={height && Number(height)}
        theme={theme || 'light'}
        padding={appendPadding || 'auto'}
        data={finalData}
        title={{
          visible: !!chartTitle,
          text: chartTitle || '',
          style: {
            fontSize: 14,
            color: 'var(--PrimaryColor)',
          },
        }}
        xField={xField || 'year'}
        yField={yField || 'value'}
        seriesField={seriesField}
        color={(colorType && colorType['color']) || ''}
        legend={{
          visible: isLegend,
          position: legendPosition,
        }}
        interactions={
          isSlider
            ? [
                {
                  type: 'slider',
                  cfg: {
                    start: 0,
                    end: 1,
                  },
                },
              ]
            : []
        }
        {...advConf}
      />
    )
  }
}

export default BChart
