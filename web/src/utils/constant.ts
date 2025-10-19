export const DATE_TIME_FORMAT = 'YYYY-MM-DD HH:mm:ss';
export const DATE_FORMAT = 'YYYY-MM-DD';
export const TIME_FORMAT = 'HH:mm:ss';

export const MAX_FILE_SIZE_MB = 10;
export const MAX_FILE_SIZE_MB_X2 = 20;

export const UPLOAD_TEST_FILE_KEY = 'angusTesterCaseAttachments';
export const UPLOAD_ISSUE_FILE_KEY = 'angusTesterTaskAttachments';

export const SETTING_KEYS = {
  MAX_RESOURCE_ACTIVITIES: 'MAX_RESOURCE_ACTIVITIES'

  // Other configuration keys can be added here
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

export const CONTENT_TYPE = {
  FORM_URLENCODED: 'application/x-www-form-urlencoded',
  MULTIPART_FORM_DATA: 'multipart/form-data',
  JSON: 'application/json',
  HTML: 'text/html',
  XML: 'application/xml',
  JAVASCRIPT: 'application/javascript',
  TEXT_PLAIN: 'text/plain',
  WILDCARD: '*/*'
} as const;

export const RADIO_TYPE = {
  NONE: 'none',
  FORM_URLENCODED: 'application/x-www-form-urlencoded',
  MULTIPART_FORM_DATA: 'multipart/form-data',
  OCTET_STREAM: 'application/octet-stream',
  RAW: 'raw'
} as const;

export const LANGUAGE = {
  JSON: 'json',
  HTML: 'html',
  TYPESCRIPT: 'typescript',
  TEXT: 'text'
} as const;

// HTTP Header constants
export const HTTP_HEADERS = {
  CONTENT_TYPE: 'Content-Type',
  ACCEPT: 'accept',
  COOKIE: 'Cookie',
  SET_COOKIE: 'Set-Cookie',
  SET_COOKIE_LOWER: 'set-cookie'
} as const;
