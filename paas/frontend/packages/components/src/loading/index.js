import React, { Component } from 'react'
import classNames from 'classnames'
import PropTypes from 'prop-types'

export class Loading extends Component {
  render() {
    const { platformName } = this.props
    return (
      <div className={'sw-loading'}>
        <div className={'sw-loading-center'}>
          <div className={'sw-loading-center-absolute'}>
            <div
              className={classNames(
                'sw-loading-center-absolute-object',
                'sw-loading-center-absolute-four',
              )}
            />
            <div
              className={classNames(
                'sw-loading-center-absolute-object',
                'sw-loading-center-absolute-three',
              )}
            />
            <div
              className={classNames(
                'sw-loading-center-absolute-object',
                'sw-loading-center-absolute-two',
              )}
            />
            <div
              className={classNames(
                'sw-loading-center-absolute-object',
                'sw-loading-center-absolute-one',
              )}
            />
          </div>
          <h1 className={'sw-loading-center-text'}>
            <span>{platformName}</span>
          </h1>
          <h1 className={'sw-loading-center-text'}>
            <span>{platformName}</span>
          </h1>
        </div>
      </div>
    )
  }
}

Loading.propTypes = {
  platformName: PropTypes.string,
}
