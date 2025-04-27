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
