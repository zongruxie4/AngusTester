import { select } from 'xpath-next/dist/xpath';

const isValidRegular = (regexp: string): boolean => {
  try {
    RegExp(regexp);
    return true;
  } catch (error) {
    return false;
  }
};

const isValidXPath = (xpath: string): boolean => {
  try {
    const doc = new DOMParser().parseFromString('<root></root>', 'text/xml');
    select(xpath, doc, false);
    return true;
  } catch (error) {
    return false;
  }
};

// @TODO 暂不实现
const isValidJSONPath = (jsonpath: string): boolean => {
  if (jsonpath) {
    return true;
  }

  return true;
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
const splitTime = (str: string): [string, string] => {
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

const getCurrentDuration = (_currentDurationStr, _durationStr) => {
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

const formatTime = (timestamp:number):string => {
  const second = 1000;
  const minute = 60 * second;
  const hour = 60 * minute;
  if (timestamp < second) {
    return timestamp + '毫秒';
  }

  if (timestamp < minute) {
    return timestamp / second + '秒';
  }

  if (timestamp < hour) {
    const remainder = timestamp % minute;
    if (remainder === 0) {
      return timestamp / minute + '分';
    }

    return Math.floor(timestamp / minute) + '分' + remainder / second + '秒';
  }

  const remainder = timestamp % hour;
  if (remainder === 0) {
    return timestamp / hour + '小时';
  }

  let suffix = '';
  if (remainder < hour) {
    const _remainder = remainder % minute;
    if (_remainder === 0) {
      suffix += remainder / minute + '分';
    } else {
      suffix += Math.floor(remainder / minute) + '分' + _remainder / second + '秒';
    }
  }

  return Math.floor(timestamp / hour) + '小时' + suffix;
};

export { isValidRegular, isValidXPath, isValidJSONPath, getCurrentDuration, splitTime, formatTime };
