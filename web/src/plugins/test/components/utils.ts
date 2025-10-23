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
  min: t('chart.min'),
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


export interface ValueItem {
  name: string;
  cvsValue: string;
  [key:string]: string|number;
}

export interface ListData {
  values: ValueItem[];
  timestamp: string;
}

interface ExtractedData {
  name: string;
  data: number[];
}

// c 参数：表示要被转化的容量大小，以字节为单
// b 参数：表示如果转换时出小数，四舍五入保留多少位 默认为2位小数
export const formatBytes = (size = 0, decimal = 2): string => {
  if (+size === 0) return '0 B';
  const c = 1024;
  const e = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
  const f = Math.floor(Math.log(size) / Math.log(c));
  return parseFloat((size / Math.pow(c, f)).toFixed(decimal)) + ' ' + e[f];
};

export const useExecCount = () => {
  const convertCvsValue = (list: ListData[], cvsKey: string[], handler?: (key: string, value: number) => void): ListData[] => {
    return list.map((item) => {
      const updatedValues = item.values.map((valueItem) => {
        const values = valueItem.cvsValue ? valueItem.cvsValue.split(',') : Array(cvsKey.length).fill('0');
        const updatedValuePairs = cvsKey.reduce((acc, key, index) => {
          acc[key] = parseFloat(values[index]) || 0;
          if (typeof handler === 'function') {
            handler(key, acc[key]);
          }
          return acc;
        }, {} as Record<string, number>);

        return {
          ...valueItem,
          ...updatedValuePairs
        };
      });

      return {
        ...item,
        values: updatedValues
      };
    });
  };

  const extractData = (cvsKey: string, dataList: ListData[]): ExtractedData[] => {
    const result: ExtractedData[] = [];
    dataList.forEach((data) => {
      const values = data.values;
      values.forEach((value) => {
        if (cvsKey in value) {
          const existingItem = result.find((item) => item.name === value.name);
          if (existingItem) {
            existingItem.data.push(value[cvsKey]);
          } else {
            result.push({
              name: value.name,
              data: [value[cvsKey]]
            });
          }
        }
      });
    });
    return result;
  };

  return {
    convertCvsValue, extractData
  };
};


export const generateCopyName = (strings:string[], str:string) => {
  // 检查数组中是否包含给定字符串
  if (!strings.includes(str)) {
    return str;
  }

  let maxCopyNumber = 0;

  // 检查数组中是否包含以 "copy" 结尾的字符串，如果包含，则找到最大的数字
  // 同时找到以 "copy" +数字 结尾的字符串中最大的数字
  for (let i = 0; i < strings.length; i++) {
    const s = strings[i];
    if (s.startsWith(str) && s.match(/copy\d*/)) {
      let copyNumber = parseInt(s.replace(`${str} copy`, ''));
      if (isNaN(copyNumber)) {
        copyNumber = 1;
      }
      if (copyNumber > maxCopyNumber) {
        maxCopyNumber = copyNumber;
      }
    }
  }

  // 如果存在以 "copy" 结尾的字符串，则返回该字符串加上最大数字加 1
  if (maxCopyNumber > 0) {
    return `${str} copy${maxCopyNumber + 1}`;
  }

  // 返回字符串加上 "copy"
  return `${str} copy`;
};

export { formatTime, getCurrentDuration, splitTime };