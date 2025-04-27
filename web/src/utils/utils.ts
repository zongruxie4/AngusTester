import dayjs from 'dayjs';

export const sizeUnitFormat = (value: number, unit?: 'B' | 'KB' | 'MB' | 'GB' | 'TB' | 'PB' | 'EB' | 'ZB' | 'YB'): string => {
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
  const i = parseInt(`${Math.log(value) / Math.log(k)}`);
  if (unit) {
    const index = sizes.indexOf(unit);
    return Math.round((value / Math.pow(k, index))) + unit;
  } else {
    return Math.round((value / Math.pow(k, i))) + sizes[i];
  }
};

// c 参数：表示要被转化的容量大小，以字节为单
// b 参数：表示如果转换时出小数，四舍五入保留多少位 默认为2位小数
export const formatBytes = (size = 0, decimal = 2): string => {
  if (size === 0) return '0B';
  const c = 1024;
  const e = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
  const f = Math.floor(Math.log(size) / Math.log(c));
  return parseFloat((size / Math.pow(c, f)).toFixed(decimal)) + e[f];
};

type Fn = (ele: unknown, index: number) => boolean

/**
 * 从数组中找出符合条件的最后一个元素的下标，没有找到返回-1
 * @param callback
 */
export const findLastIndex = (arr: unknown[], callback: Fn): number => {
  if (!arr?.length) {
    return -1;
  }

  for (let i = arr.length; i--;) {
    if (typeof callback === 'function') {
      if (callback(arr[i], i)) {
        return i;
      }
    }
  }

  return -1;
};

export const getCurrentPage = (pageNo:number, pageSize:number, total:number):number => {
  if (pageNo === 1 || pageSize >= total) {
    return 1;
  }

  const remainder = total % pageSize;
  if (remainder === 1) {
    const totalPage = Math.ceil(total / pageSize);
    if (totalPage === pageNo) {
      return pageNo - 1;
    }
  }

  return pageNo;
};

export const durationUnitOpt = [
  { label: '小时', value: 'h' }, { label: '分', value: 'min' }, { label: '秒', value: 's' }, { label: '豪秒', value: 'ms' }
];

export const getDurationUnit = (unit: 's'|'ms'|'min'|'h'|'d') => {
  switch (unit) {
    case 's':
      return 'Second';
    case 'ms':
      return 'Millisecond';
    case 'min':
      return 'Minute';
    case 'h':
      return 'Hour';
    case 'd':
      return 'Day';
    default: return 'Second';
  }
};

export const splitDuration = (duration: string): string[] => {
  if (duration.includes('h')) {
    duration = duration.replace('h', '');
    return [duration, 'h'];
  } else if (duration.includes('ms')) {
    duration = duration.replace('ms', '');
    return [duration, 'ms'];
  } else if (duration.includes('min')) {
    duration = duration.replace('min', '');
    return [duration, 'min'];
  } else if (duration.includes('s')) {
    duration = duration.replace('s', '');
    return [duration, 's'];
  } else if (duration.includes('d')) {
    duration = duration.replace('d', '');
    return [duration, 'd'];
  }
  return [];
};

const getRandomIntInclusive = (min:number, max:number):number => {
  min = Math.ceil(min);
  max = Math.floor(max);
  return Math.floor(Math.random() * (max - min + 1)) + min;
};

export const randomString = (len = 9): string => {
  const num = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9];
  const lowercase = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'];
  const uppercase = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'tag', 'V', 'W', 'X', 'Y', 'Z'];
  const metaArr = [num, lowercase, uppercase];
  let index = 0;
  let average = Math.floor(len / 3);
  const tempArr = metaArr.reduce((prev, cur, curIndex) => {
    if (curIndex === 2) {
      average = len - index;
    }

    for (let i = 0; i < average; i++) {
      prev.push(cur[getRandomIntInclusive(0, cur.length - 1)]);
      index++;
    }

    prev.sort(() => { return 0.5 - Math.random(); });
    return prev;
  }, [] as (number|string)[]);

  return tempArr.join('');
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

export const getDateArr = (size = 7) => {
  const dates: string[] = [];
  for (let i = 0; i < size; i++) {
    // 获取当前日期往前减去i天的日期
    const date = dayjs().subtract(i, 'day').format('YYYY-MM-DD');
    dates.push(date);
  }
  return dates;
};

export const getDateArrWithTime = (start, end) => {
  const dates: string[] = [];
  while (!dates[0] || (dayjs(dates[0]).isAfter(dayjs(start)))) {
    if (!dates[0]) {
      const date = dayjs(end).format('YYYY-MM-DD');
      dates.unshift(date);
    } else {
      const date = dayjs(dates[0]).subtract(1, 'day').format('YYYY-MM-DD');
      dates.unshift(date);
    }
  }
  return dates;
};
