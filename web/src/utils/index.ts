import { Ref } from 'vue';

// 处理表单校验方法
export const validFields = (formRef: Ref, fields: string[]): void => {
  formRef.value.validateFields(fields).then(() => {
    formRef.value.clearValidate(fields);
  }, () => { /** */ });
};

// TODO 使用全局工具方法代替
export const formatBytes = (size = 0, decimal = 2): string => {
  if (size === 0) return '0 B';
  const c = 1024;
  const e = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
  const f = Math.floor(Math.log(size) / Math.log(c));
  return parseFloat((size / Math.pow(c, f)).toFixed(decimal)) + ' ' + e[f];
};

type HeadersType = {
  paramsType?: boolean,
  headers: { 'XC-Opt-Tenant-Id'?: string }
}

// 处理 headers 中的租户ID参数
export const handleHeaders = (tenantId: string, paramsType?: boolean): HeadersType => {
  const headers: HeadersType = {
    headers: {}
  };
  if (tenantId) {
    headers.headers['XC-Opt-Tenant-Id'] = tenantId;
  }
  if (paramsType) {
    headers.paramsType = paramsType;
  }
  return headers;
};

/**
 * 根据秒数获取时、分、秒
 * @param num 总秒数
 * @param unit 想要获取的单位
 * @param isShow 是否是展示, 如果是, 则小于10需要补0
 * @returns number | string
 */
export const getUnitTimeNumber = (num: number, unit: 'h' | 'm' | 's', isShow = false): number | string => {
  if (unit === 'h') {
    const h = Math.floor(num / 3600);
    return isShow && h < 10 ? `0${h}` : h;
  } else if (unit === 'm') {
    const m = Math.floor(num % 3600 / 60);
    return isShow && m < 10 ? `0${m}` : m;
  } else if (unit === 's') {
    const s = num % 3600 % 60;
    return isShow && s < 10 ? `0${s}` : s;
  }
  return num;
};

/**
 * 获取随机数
 * @param min 最小值
 * @param max 最大值
 * @returns number
 */
export const randomNum = (min: number, max: number): number => {
  if (min && !max) {
    return parseInt(`${Math.random() * min + 1}`, 10);
  }
  if (min && max) {
    return parseInt(`${Math.random() * (max - min + 1) + min}`, 10);
  }
  return 0;
};

const letterMap = {
  ms: '毫秒',
  s: '秒',
  min: '分钟',
  h: '小时',
  d: '天'
};

/**
 * @param '10s'、'20min'、'5h'这样的字符串
 * @returns ['10','秒']拆分后并返回中文单位的数组
 */
export const splitTime = (str: string): [string, string] => {
  const number = str.replace(/\D/g, '');
  const unit = str.replace(/\d/g, '');
  return [number, letterMap[unit]];
};

/**
 * @param _currentDurationStr 当前执行时长
 * @param _durationStr 执行时长
 * @returns 当前执行时长和执行时长统一单位后的时间，单位以执行时长的单位为准
 * @reamrk _currentDurationStr有值，_durationStr必须有值
 */

export const getCurrentDuration = (_currentDurationStr, _durationStr) => {
  if (!_currentDurationStr) {
    return '--';
  }

  const _durationUnit = _durationStr.replace(/\d/g, '');
  const _currentDuration = +splitTime(_currentDurationStr)[0];

  // 如果时长是秒
  if (_durationUnit === 's') {
    // 当前时长是小时
    if (_currentDurationStr.includes('h')) {
      return keepTwoDecimals(_currentDuration * 60 * 60);
    }

    // 当前时长是分钟
    if (_currentDurationStr.includes('min')) {
      return keepTwoDecimals(_currentDuration * 60);
    }

    return _currentDuration;
  }

  // 如果时长是分钟
  if (_durationUnit === 'min') {
    if (_currentDurationStr.includes('h')) {
      return keepTwoDecimals(_currentDuration * 60);
    }

    if (_currentDurationStr.includes('s')) {
      return keepTwoDecimals(_currentDuration / 60);
    }

    return _currentDuration;
  }

  // 如果时长是小时
  if (_currentDurationStr.includes('s')) {
    return keepTwoDecimals(_currentDuration / 60 / 60);
  }

  if (_currentDurationStr.includes('min')) {
    return keepTwoDecimals(_currentDuration / 60);
  }

  return _currentDuration;
};

const keepTwoDecimals = (num) => {
  if (num % 1 !== 0 && num.toString().split('.')[1].length > 2) {
    return num.toFixed(2);
  } else {
    return num;
  }
};
