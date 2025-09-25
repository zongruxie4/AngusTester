import { i18n } from '@xcan-angus/infra';
const I18nInstance = i18n.getI18n();
const t = I18nInstance?.global?.t || ((value: string):string => value);

const formatTime = (timestamp:number):string => {
  const second = 1000;
  const minute = 60 * second;
  const hour = 60 * minute;
  if (timestamp < second) {
    return timestamp + t('commonPlugin.utils.formatTime.milliseconds');
  }

  if (timestamp < minute) {
    return timestamp / second + t('commonPlugin.utils.formatTime.seconds');
  }

  if (timestamp < hour) {
    const remainder = timestamp % minute;
    if (remainder === 0) {
      return timestamp / minute + t('commonPlugin.utils.formatTime.minutes');
    }

    return Math.floor(timestamp / minute) + t('commonPlugin.utils.formatTime.minutes') + remainder / second + t('commonPlugin.utils.formatTime.seconds');
  }

  const remainder = timestamp % hour;
  if (remainder === 0) {
    return timestamp / hour + t('commonPlugin.utils.formatTime.hours');
  }

  let suffix = '';
  if (remainder < hour) {
    const _remainder = remainder % minute;
    if (_remainder === 0) {
      suffix += remainder / minute + t('commonPlugin.utils.formatTime.minutes');
    } else {
      suffix += Math.floor(remainder / minute) + t('commonPlugin.utils.formatTime.minutes') + _remainder / second + t('commonPlugin.utils.formatTime.seconds');
    }
  }

  return Math.floor(timestamp / hour) + t('commonPlugin.utils.formatTime.hours') + suffix;
};

const letterMap = {
  ms: t('commonPlugin.utils.timeUnits.ms'),
  s: t('commonPlugin.utils.timeUnits.s'),
  min: t('commonPlugin.utils.timeUnits.min'),
  h: t('commonPlugin.utils.timeUnits.h'),
  d: t('commonPlugin.utils.timeUnits.d')
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
    return t('common.defaultValue');
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

export { formatTime, getCurrentDuration, splitTime };
