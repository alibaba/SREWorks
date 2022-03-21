/**
 * @author caoshuaibiao
 * @date 2021/8/23 20:01
 * @Description:内置业务组件包装器
 */
import React from "react";
import  OamWidget from '../../../OamWidget';

function BuiltInBusiness(props) {
    let {widgetConfig = {},...otherProps} = props;
    console.log(props,'props-内置业务')
    let {businessType,businessConfig} = widgetConfig;
    return <OamWidget {...otherProps} widget={{type:businessType,config:businessConfig}}/>;

}

export default BuiltInBusiness;