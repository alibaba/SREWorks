import * as React from 'react'
import { Tag, Tooltip, Button, Modal } from 'antd'
import _ from 'lodash'

function AntdTags(props) {
  let tags = []
  const colors = [
    '#90ee90',
    '#2191ee',
    '#9a69ee',
    '#41ee1a',
    '#484aee',
    '#6B8E23',
    '#48D1CC',
    '#3CB371',
    '#388E8E',
    '#1874CD',
  ]
  let {
    value,
    paramsFormat = {},
    isRandomColor = false,
    color,
    autoTheme = false,
    tooltipConfig = false,
    modalConfig,
  } = props
  if (_.isString(value)) {
    tags = value.split(',')
  } else if (_.isArray(value)) {
    value.map((item, index) => {
      if (_.isString(item)) {
        tags.push(item)
      } else {
        let tagText = item[paramsFormat['tagText']] || item.name || '-'
        let subText = item[paramsFormat['subText']]
        tags.push({ text: tagText + (subText ? '(' + subText + ')' : ''), color: item.color })
      }
    })
  }
  if (autoTheme) {
    color = colors[Math.floor(Math.random() * colors.length)]
  }
  if (tooltipConfig) {
    return (
      <span>
        {tags.map((item, r) => (
          <Tooltip title={item.text || item}>
            <Tag
              style={{
                textOverflow: 'ellipsis',
                maxWidth: tooltipConfig.maxLength,
                overflow: 'hidden',
              }}
              color={isRandomColor ? colors[r % 10] : color || item.color}
              key={r}
            >
              {item.text || item}
            </Tag>
          </Tooltip>
        ))}
      </span>
    )
  }
  if (modalConfig) {
    return (
      <Button
        type="link"
        onClick={() =>
          Modal.info({
            title: '',
            icon: '',
            bodyStyle: {
              height: 420,
              overflowX: 'scroll',
              overflowY: 'scroll',
            },
            width: 720,
            content: (
              <div>
                {tags.map((item, r) => (
                  <Tag
                    style={{ marginTop: 10 }}
                    color={isRandomColor ? colors[r % 10] : color || item.color}
                    key={r}
                  >
                    {item.text || item}
                  </Tag>
                ))}
              </div>
            ),
          })
        }
      >
        {tags.length}
      </Button>
    )
  }
  return (
    <span>
      {tags.map((item, r) => (
        <Tag color={isRandomColor ? colors[r % 10] : color || item.color} key={r}>
          {item.text || item}
        </Tag>
      ))}
    </span>
  )
}

export default AntdTags
