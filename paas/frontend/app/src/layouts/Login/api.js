import { httpClient } from '@sreworks/shared-utils'
const apiPrefix = 'gateway/v2/common/authProxy/'
export function loginUserData() {
  return httpClient.get(apiPrefix + 'auth/user/info').then((res) => {
    return res ? res : []
  })
}
export function getLoginOption(aliyunId) {
  return httpClient.get(apiPrefix + 'auth/private/account/login/option', {
    aliyunId: aliyunId,
  })
}

export function getAccoutLoginSms(aliyunId, password) {
  return httpClient.post(
    apiPrefix + 'auth/private/account/login/sms',
    {
      aliyunId: aliyunId,
      password: password,
    },
    { 'Content-Type': 'application/json;charset=UTF-8' },
  )
}
export function getAccoutLoginTo(aliyunId, password, lang, smsCode) {
  return httpClient.post(
    apiPrefix + 'auth/private/account/login',
    {
      aliyunId: aliyunId,
      password: password,
      lang: lang,
      smsCode: smsCode,
    },
    { 'Content-Type': 'application/json;charset=UTF-8' },
  )
}
export function getAccoutLogin(loginName, password) {
  return httpClient.post(apiPrefix + 'auth/login', {
    loginName: loginName,
    password: password,
  })
}

export function getUserLang() {
  //语言列表获取 httpClient
  return httpClient.get(apiPrefix + 'auth/user/lang', {})
}

export function getAccountCreate(aliyunId, password, phone, passwordChangeRequired) {
  // 创建云账号
  return API.post(
    'auth/private/account/add?appId=bcc',
    {
      aliyunId: aliyunId,
      password: password,
      phone: phone !== 'undefined' ? phone : '',
      passwordChangeRequired: passwordChangeRequired,
    },
    { 'Content-Type': 'application/json' },
  ).then((res) => {
    // console.log(res)
    return res ? res.data : {}
  })
}
export function upDataPassword(aliyunId, password) {
  // 修改密码
  return API.post(
    'auth/private/account/password/change?appId=bcc',
    {
      aliyunId: aliyunId,
      password: password,
    },
    { 'Content-Type': 'application/json;charset=UTF-8' },
  ).then((res) => {
    return res ? res.data : {}
  })
}

export function accountInfoChange(aliyunId, phone) {
  // 账号信息修改
  return API.post(
    'auth/private/account/info/change?appId=bcc',
    {
      aliyunId: aliyunId,
      phone: phone ? phone : '',
    },
    { 'Content-Type': 'application/json;charset=UTF-8' },
  ).then((res) => {
    return res ? res.data : {}
  })
}

export function SmsRegister(endpoint, token) {
  // 注册短信网关
  return API.post(
    'auth/private/gateway/sms/register',
    {
      endpoint: endpoint,
      token: token,
      appId: 'bcc',
    },
    { 'Content-Type': 'application/json;charset=UTF-8' },
  ).then((res) => {
    return res ? res.data : {}
  })
}
