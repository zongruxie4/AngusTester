export const SETTING_KEYS = {
  // 活动相关配置
  MAX_RESOURCE_ACTIVITIES: 'MAX_RESOURCE_ACTIVITIES',
  
  // 其他配置键可以在这里添加
  // API_LOG_CONFIG: 'API_LOG_CONFIG',
  // COMMON_DATA_PERMISSION: 'COMMON_DATA_PERMISSION',
  // HEALTH_CHECK: 'HEALTH_CHECK',
  // LOCALE: 'LOCALE',
  // MAX_METRICS_DAYS: 'MAX_METRICS_DAYS',
  // OPERATION_LOG_CONFIG: 'OPERATION_LOG_CONFIG',
  // PREF_INDICATOR: 'PREF_INDICATOR',
  // QUOTA: 'QUOTA',
  // SECURITY: 'SECURITY',
  // SOCIAL: 'SOCIAL',
  // STABILITY_INDICATOR: 'STABILITY_INDICATOR',
  // SYSTEM_LOG_CONFIG: 'SYSTEM_LOG_CONFIG',
  // THEME: 'THEME'
} as const;

export const cropperUploadOption = {
  outputSize: 1,
  outputType: 'png',
  info: true,
  canScale: true,
  autoCrop: true,
  autoCropWidth: '280',
  autoCropHeight: '280',
  fixed: true,
  fixedNumber: [1, 1],
  full: false,
  fixedBox: false,
  canMove: true,
  canMoveBox: true,
  original: false,
  centerBox: false,
  high: true,
  infoTrue: false,
  maxImgSize: 2000,
  enlarge: 1,
  mode: 'contain',
  preW: 0,
  limitMinSize: [100, 100]
};
