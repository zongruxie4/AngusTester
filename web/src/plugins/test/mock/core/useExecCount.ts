
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
  if (size === 0) return '0 B';
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
