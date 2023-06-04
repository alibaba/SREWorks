/**
 * Created by caoshuaibiao on 2019/1/22.
 * 表单元素创建工厂
 */
import React from 'react'
import { QuestionCircleOutlined } from '@ant-design/icons'
import { Form } from '@ant-design/compatible'
import '@ant-design/compatible/assets/index.css'
import 'rc-color-picker/assets/index.css'
import {
  Input,
  TreeSelect,
  DatePicker,
  Select,
  TimePicker,
  Cascader,
  InputNumber,
  Radio,
  Tooltip,
  Checkbox,
  Switch,
  Slider,
  Row,
  Col,
} from 'antd'

import FileUpload from './FormItem/FileUpload'
import ImageUpload from './FormItem/ImageUpload'
import GroupFormItem from './FormItem/GroupFormItem'
import FormElementType from './FormElementType'
import SelectInput from './FormItem/SelectInput'
import JSONEditor from './FormItem/JSONEditor'
import TableItem from './FormItem/TableItem'
import AceViewer from '../AceViewer'
import CascadeGroup from './FormItem/CascadeGroup'
// import OamWidgetItem from './FormItem/OamWidgetItem'
import SelectItemWrapper from './FormItem/SelectItemWrapper'
import { localeHelper } from '@sreworks/shared-utils'
import JSXRender from '../JSXRender'
import DatePickerWrapper from './FormItem/DatePickerWrapper'
import JSONSchemaItem from './FormItem/JSONSchemaItem'
import GridCheckBoxWrapper from './FormItem/GridCheckBoxWrapper'
import PopoverAceEditor from './FormItem/PopoverAceEditor'
import SliderItem from './FormItem/SliderItem'
import ColorPicker from 'rc-color-picker'
import HandleTag from './FormItem/HandleTag'
import EnhancedInput from './FormItem/EnhancedInput'
import FileUploadNoDefer from './FormItem/FileUploadNoDefer'
import FileUploadSingle from './FormItem/FileUploadSingle'
import EditTableCut from '../EditableCut'
import SRECron from '../SRECron'
import DynamicForm from '../Dynamic'
import IconSelector from './FormItem/IconSelector'

const FormItem = Form.Item
const { Option } = Select
const { RangePicker } = DatePicker
const { TextArea } = Input
const RadioGroup = Radio.Group
const CheckboxGroup = Checkbox.Group

export default class FormElementFactory {
  static createFormItem(item, form, itemLayout) {
    const { getFieldDecorator } = form
    let hidden = false
    if (item.isHidden && item.isHidden === 1) {
      hidden = true
    } else {
      hidden = false
    }
    let formItemLayout = itemLayout || {
      labelCol: {
        xs: { span: 24 },
        sm: { span: 6 },
      },
      wrapperCol: {
        xs: { span: 24 },
        sm: { span: 12 },
        md: { span: 10 },
      },
    }
    let label = item.label
    let mark = item.mark ? '  (' + item.mark + ')' : null

    let inputRule = {
      required: item.required,
      message: item.label + localeHelper.get('common.notNull', '不能为空'),
    }
    //自定义规则
    let customRules = []
    //存在正则表达式
    if (item.validateReg) {
      customRules.push({
        validator: (rule, value, callback) => {
          if (value) {
            let regDef = item.validateReg.split('##')
            let valueReg = new RegExp(regDef[0])
            if (!valueReg.test(value)) {
              callback(
                regDef[1]
                  ? regDef[1]
                  : localeHelper.get('input.unqualified', '输入值不符合业务规则要求!'),
              )
            } else {
              callback()
            }
          } else {
            callback()
          }
        },
      })
    }
    let itemElement = null,
      innerFormItem = true,
      { defModel = {} } = item
    let { needOption, remote, enableSelectAll, prefix, addonBefore, addonAfter, suffix } = defModel
    let needSelectWrapper = needOption || remote || enableSelectAll
    if (!item.label) {
      formItemLayout = {
        wrapperCol: {
          xs: { span: 24 },
          sm: { span: 24 },
          md: { span: 24 },
          lg: { span: 24 },
          xl: { span: 24 },
          xxl: { span: 24 },
        },
      }
    }
    let cssObj = { visibility: hidden ? 'hidden' : 'visible' }
    if (hidden) {
      cssObj = Object.assign({}, cssObj, { height: 0, margin: 0 })
    }
    if (item.tooltip) {
      label = (
        <span key={item.label} style={{ ...cssObj }}>
          {item.label}
          <em className="optional">
            {mark}
            <Tooltip title={<JSXRender jsx={item.tooltip} />}>
              <QuestionCircleOutlined style={{ marginRight: 4, marginLeft: 4 }} />
            </Tooltip>
          </em>
        </span>
      )
    }
    switch (item.type) {
      case FormElementType.INPUT: //普通输入框
        if (item.validateType === 'number') {
          itemElement = (
            <InputNumber
              {...defModel}
              style={{ width: '100%', ...cssObj }}
              placeholder={
                defModel.inputTip
                  ? defModel.inputTip
                  : localeHelper.get('common.placeInput', '请输入') + item.label
              }
              autoComplete="off"
            />
          )
        } else {
          itemElement = (
            <Input
              style={{ ...cssObj }}
              disabled={item.disabled || false}
              prefix={prefix && <JSXRender jsx={prefix} />}
              addonBefore={addonBefore && <JSXRender jsx={addonBefore} />}
              addonAfter={addonAfter && <JSXRender jsx={addonAfter} />}
              suffix={suffix && <JSXRender jsx={suffix} />}
              placeholder={
                defModel.inputTip || item.tooltip || item.inputTip
                  ? defModel.inputTip || item.tooltip || item.inputTip
                  : localeHelper.get('common.placeInput', '请输入') + item.label
              }
              autoComplete="off"
            />
          )
        }
        break
      case FormElementType.HIDDEN_INPUT: //hidden input
        itemElement = <Input type="hidden" />
        break
      case FormElementType.ENHANCED_INPUT: //增强输入框
        itemElement = (
          <EnhancedInput
            style={{ ...cssObj }}
            defModel={defModel}
            form={form}
            item={item}
            style={{ width: '100%', ...cssObj }}
          />
        )
        break
      case FormElementType.TEXTAREA: //textarea
        itemElement = (
          <TextArea
            style={{ minHeight: 32, ...cssObj }}
            placeholder={
              defModel.inputTip || item.tooltip
                ? defModel.inputTip || item.tooltip
                : localeHelper.get('common.placeInput', '请输入') + item.label
            }
            autoSize={{ minRows: item.row || 3 }}
            autoComplete="off"
          />
        )
        break
      case FormElementType.SELECT: //单选
        if (needSelectWrapper) {
          itemElement = (
            <SelectItemWrapper style={{ ...cssObj }} item={item} selectType="select" form={form} />
          )
          break
        }
        let options = []
        let optionsValue = item.optionValues || []
        optionsValue.forEach(function (option) {
          options.push(
            <Option key={option.value} value={option.value}>
              {option.label}
            </Option>,
          )
        })
        itemElement = (
          <Select
            showSearch
            allowClear={true}
            optionFilterProp="children"
            style={{ ...cssObj }}
            type="hidden"
            placeholder={
              defModel.inputTip || item.inputTip
                ? defModel.inputTip || item.inputTip
                : localeHelper.get('pleaseChoose', '请选择') + item.label
            }
          >
            {options}
          </Select>
        )
        break
      case FormElementType.MULTI_SELECT: //多选
        if (needSelectWrapper) {
          itemElement = (
            <SelectItemWrapper style={{ ...cssObj }} item={item} selectType="select" form={form} />
          )
          break
        }
        let moptions = []
        let moptionsValue = item.optionValues || []
        moptionsValue.forEach(function (option) {
          moptions.push(
            <Option key={option.label} value={option.value}>
              {option.label}
            </Option>,
          )
        })
        itemElement = (
          <Select
            showSearch
            style={{ ...cssObj }}
            mode="multiple"
            allowClear={true}
            optionFilterProp="children"
            placeholder={
              defModel.inputTip || item.inputTip
                ? defModel.inputTip || item.inputTip
                : localeHelper.get('common.placeInput', '请输入') + item.label
            }
          >
            {moptions}
          </Select>
        )
        break
      case FormElementType.DATE: //普通日期
        itemElement = (
          <DatePickerWrapper
            item={item}
            style={{ width: '100%', ...cssObj }}
            format={window.APPLICATION_DATE_FORMAT || 'YYYY-MM-DD HH:mm'}
          />
        )
        break
      case FormElementType.DATA_RANGE: //普通时间范围
        itemElement = (
          <DatePickerWrapper
            item={item}
            type="range"
            style={{ width: '100%', ...cssObj }}
            placeholder={['', '']}
            format={window.APPLICATION_DATE_FORMAT || 'YYYY-MM-DD HH:mm'}
            showTime={{ format: 'HH:mm' }}
          />
        )
        break
      case FormElementType.DISABLED_INPUT: //hidden input
        itemElement = <Input model={item} disabled={true} />
        break
      case FormElementType.INPUT_NUMBER:
        itemElement = (
          <InputNumber
            model={item}
            style={{ width: '100%', ...cssObj }}
            placeholder={
              defModel.inputTip
                ? defModel.inputTip
                : localeHelper.get('common.placeInput', '请输入') + item.label
            }
          />
        )
        break
      case FormElementType.DISABLED_TEXTAREA: //textarea
        itemElement = (
          <TextArea
            disabled={true}
            style={{ minHeight: 32, ...cssObj }}
            placeholder={
              defModel.inputTip || item.tooltip
                ? defModel.inputTip || item.tooltip
                : localeHelper.get('common.placeInput', '请输入') + item.label
            }
            autoSize={{ minRows: item.row || 3 }}
            autoComplete="off"
          />
        )
        break
      case FormElementType.SELECT_TAG: //可输入标签
        let tags = []
        let tagsOption = item.optionValues || []
        tagsOption.forEach(function (tag) {
          tags.push(
            <Option key={tag.value || tag} value={tag.value || tag}>
              {tag.label || tag}
            </Option>,
          )
        })
        itemElement = (
          <Select
            mode="tags"
            style={{ ...cssObj }}
            allowClear={true}
            //tokenSeparators={[","]}
            placeholder={item.inputTip || defModel.inputTip}
          >
            {tags}
          </Select>
        )
        break
      case FormElementType.SELECT_TREE: //选择树
        itemElement = (
          <TreeSelect
            style={{ ...cssObj }}
            treeData={Array.isArray(item.treeData) ? item.treeData : [item.treeData]}
            treeCheckable="true"
            showCheckedStrategy={TreeSelect.SHOW_PARENT}
          />
        )
        break
      case FormElementType.RADIO: //radio单选
        itemElement = <RadioGroup style={{ ...cssObj }} options={item.optionValues} />
        break
      case FormElementType.CHECKBOX: //checkbox多选
        itemElement = <CheckboxGroup style={{ ...cssObj }} options={item.optionValues} />
        break
      case 12:
        itemElement = (
          <TreeSelect
            style={{ ...cssObj }}
            treeData={Array.isArray(item.treeData) ? item.treeData : [item.treeData]}
            treeCheckable="true"
            showCheckedStrategy={TreeSelect.SHOW_CHILD}
            onChange={item.onChange}
          />
        )
        break
      case FormElementType.TIME_PICKER: //时间选择
        itemElement = <TimePicker style={{ width: '100%', ...cssObj }} />
        break
      case 14: //Radio.Button
        itemElement = (
          <RadioGroup style={{ ...cssObj }}>
            {item.optionValues.map((ib) => {
              return (
                <Radio.Button value={ib.value} key={ib.value}>
                  {ib.label}
                </Radio.Button>
              )
            })}
          </RadioGroup>
        )
        break
      case 15: //Slider 滑条
        //itemElement=<Slider {...defModel}  {...item}/>;
        itemElement = <SliderItem style={{ ...cssObj }} model={item} />
        break
      case FormElementType.SWITCH: //Switch 开关
        itemElement = <Switch style={{ ...cssObj }} {...defModel} {...item} />
        break
      case 17: //级联选择
        if (needOption || remote) {
          itemElement = (
            <SelectItemWrapper style={{ ...cssObj }} item={item} selectType="cascader" />
          )
          break
        }
        itemElement = <Cascader style={{ ...cssObj }} options={item.optionValues || []} {...item} />
        break
      case 18: //password
        itemElement = <Input.Password style={{ ...cssObj }} />
        break
      case 92:
        return null
      case 93: //需求单自定义作业步骤
        return null
      case 94: //changfree要求的影响应用
        return null
        break
      case 95: //changfree要求的变更类型
        return null
        break
      case 96: //影响度
        return null
      case 97:
        return null
      case 98:
        return null
      case 99:
        return null
        break
      case FormElementType.MACHINE_TARGET: //目标选择器
        return null
        break
      case FormElementType.GROUP_ITEM: //表单分组
        innerFormItem = false
        itemElement = (
          <GroupFormItem
            style={{ ...cssObj }}
            key={item.name}
            form={form}
            model={item}
            layout={formItemLayout}
          />
        )
        break
      case 87: //选择输入
        innerFormItem = false
        itemElement = (
          <FormItem {...formItemLayout} style={{ ...cssObj }} label={label} key={item.name}>
            {getFieldDecorator(item.name + 'Group', {
              initialValue: item.initValue,
              rules: [inputRule, ...customRules],
            })(<SelectInput key={item.name} form={form} model={item} layout={formItemLayout} />)}
          </FormItem>
        )
        break
      case 86: //json编辑器
        itemElement = <JSONEditor style={{ ...cssObj }} key={item.name} model={item} />
        break
      case 85: //table显示
        if (defModel.enableEdit || defModel.enableSelected) {
          innerFormItem = false
          itemElement = (
            <FormItem {...formItemLayout} style={{ ...cssObj }} label={label} key={item.name}>
              {getFieldDecorator(item.name, {
                rules: [
                  {
                    required: item.required,
                    message: localeHelper.get('common.notNull', '不能为空'),
                  },
                ],
              })(<TableItem key={item.name} model={item} />)}
            </FormItem>
          )
        } else if (defModel.enableRelated) {
          itemElement = <EditTableCut key={item.name} defModel={defModel} model={item} />
        } else {
          itemElement = (
            <TableItem
              style={{ ...cssObj }}
              defModel={defModel}
              form={form}
              key={item.name}
              model={item}
            />
          )
        }
        break
      case 84: //关联选择
        innerFormItem = false
        itemElement = (
          <CascadeGroup
            style={{ ...cssObj }}
            key={item.name}
            form={form}
            model={item}
            layout={formItemLayout}
          />
        )
        break
      case 83: //AceView
        itemElement = <AceViewer style={{ ...cssObj }} key={item.name} model={item} />
        break
      case FormElementType.ACEVIEW_JAVASCRIPT: //AceView
        itemElement = (
          <AceViewer style={{ ...cssObj }} key={item.name} model={item} mode={'javascript'} />
        )
        break
      // case 82: //运维挂件OamWidget
      //   itemElement = (
      //     <OamWidgetItem
      //       style={{ ...cssObj }}
      //       key={item.name}
      //       model={item}
      //     />
      //   )
      //   break
      case 81: //上传组件
        itemElement = <FileUpload style={{ ...cssObj }} key={item.name} model={item} />
        break
      case 811: //图片上传组件
        itemElement = <ImageUpload style={{ ...cssObj }} key={item.name} model={item} />
        break
      case FormElementType.FILE_UPLOAD_NODEFER: // 文件同步上传
        itemElement = <FileUploadNoDefer style={{ ...cssObj }} key={item.name} model={item} />
        break
      case FormElementType.FILE_UPLOAD_SINGLE:
        itemElement = <FileUploadSingle style={{ ...cssObj }} key={item.name} model={item} />
        break
      case FormElementType.IMAGE_UPLOAD_MULTI: //图片上传组件
        itemElement = (
          <ImageUpload style={{ ...cssObj }} isMulti={true} key={item.name} model={item} />
        )
        break
      case 80: //GOC对接
        return null
      case 79: //JSONSchemaForm
        innerFormItem = false
        itemElement = (
          <JSONSchemaItem
            style={{ ...cssObj }}
            key={item.name}
            form={form}
            model={item}
            layout={formItemLayout}
          />
        )
        break
      //卡片选择组件 70
      case FormElementType.GRID_CHECKBOX:
        itemElement = <GridCheckBoxWrapper style={{ ...cssObj }} key={item.name} model={item} />
        break
      case FormElementType.MODAL_ACE: //Modal Ace
        itemElement = <PopoverAceEditor style={{ ...cssObj }} key={item.name} model={item} />
        break
      case FormElementType.COLOR_PICKER: //拾色器
        itemElement = <ColorPicker style={{ ...cssObj }} key={item.name} {...item} />
        break
      case FormElementType.HANDLE_TAG:
        itemElement = <HandleTag style={{ ...cssObj }} key={item.name} {...item} />
        break
      case FormElementType.CRON:
        itemElement = <SRECron form={form} key={item.name} model={item} style={{ ...cssObj }} />
        break
      case FormElementType.DYNAMIC_FORM:
        itemElement = <DynamicForm form={form} key={item.name} model={item} style={{ ...cssObj }} />
        break
      case FormElementType.ICON_SELECTOR:
        itemElement = <IconSelector key={item.name} {...item} />
        break
      default:
        return (
          <div key="_undefined_formItem">
            {localeHelper.get('common.undefindFromEle', '未定义表单元素')}
          </div>
        )
    }
    if (innerFormItem) {
      if (item.type === 14 || item.type === 10) {
        if (item.initValue && item.initValue instanceof Array) {
          item.initValue = item.initValue.join('')
        }
      }
      itemElement = (
        <FormItem
          {...formItemLayout}
          label={label}
          style={{ ...cssObj }}
          key={item.name}
          extra={defModel && defModel.extra && <JSXRender key="_jsx_hint" jsx={defModel.extra} />}
        >
          {getFieldDecorator(item.name, {
            initialValue: item.initValue,
            rules: [inputRule, ...customRules],
          })(itemElement)}
        </FormItem>
      )
    }
    let hintMark = null
    if (defModel && defModel.jsxHint) {
      hintMark = (
        <Row style={{ marginBottom: -3, color: '#3399FF' }}>
          <Col offset={6}>
            <JSXRender key="_jsx_hint" jsx={defModel.jsxHint} />
          </Col>
        </Row>
      )
    }
    //只禁用了鼠标事件,键盘事件未禁用,在特定场景下可能会存在问题待后续完善
    if (item.readonly === 2) {
      return (
        <Tooltip
          style={{ ...cssObj }}
          title={item.tooltip ? <JSXRender jsx={item.tooltip} /> : '只读项'}
        >
          <div>
            {hintMark}
            <div style={{ pointerEvents: 'none', opacity: 0.6 }} key={item.name + '__disable'}>
              {itemElement}
            </div>
          </div>
        </Tooltip>
      )
    }
    if (hintMark) {
      return (
        <div style={{ ...cssObj }} key={item.name + '__item_con'}>
          {hintMark}
          {itemElement}
        </div>
      )
    }
    return itemElement
  }
}
