export const API_EXTENSION_KEY = {
  perfix: 'x-xc-', // 前缀
  valueKey: 'x-xc-value', // 值
  enabledKey: 'x-xc-enabled', // 启用/禁用
  exportVariableFlagKey: 'x-xc-exportVariableFlag', // 是否设置成变量
  requestSettingKey: 'x-xc-requestSetting', // 接口设置 例如超时时间等设置, object
  serverNameKey: 'x-xc-serverName', // 服务器URL名称
  serverSourceKey: 'x-xc-serverSource', // 服务器来源
  securityApiKeyPerfix: 'x-xc-apiKey', // apiKey 类型扩展
  securitySubTypeKey: 'sx-xc-securitySubType', // 安全方案子类型
  fileNameKey: 'x-xc-fileName', // 文件名称
  newTokenKey: 'x-xc-oauth2-newToken', // 是否使用生成认证令牌
  oAuth2Key: 'x-xc-oauth2-authFlow', // 生成令牌授权类型
  oAuth2Token: 'x-xc-oauth2-token', // 已有令牌 token
  formContentTypeKey: 'x-xc-contentType',
  basicAuthKey: 'x-xc-basicAuth'
} as const;
