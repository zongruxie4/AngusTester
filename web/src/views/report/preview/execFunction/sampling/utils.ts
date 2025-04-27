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

export { formatTime };
