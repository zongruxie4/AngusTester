
const uniqueArr = (arr, key) => {
  const obj = {};
  const arrays = arr.reduce((setArr, item) => {
    if (!obj[item[key]]) {
      obj[item[key]] = true;
      setArr.push(item);
    }
    return setArr;
  }, []);
  return arrays;
};

export { uniqueArr };
