
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

export const useExecCount = () => {
  const convertCvsValue = (list:{values:{name:string;cvsValue:string;}[]}[], cvsKeys:string[], pipelineKeys:string[]) => {
    if (!list?.length) {
      return [];
    }

    return list.map((item) => {
      const valuesMap = item.values.reduce((prev, cur) => {
        prev[cur.name] = cur;
        return prev;
      }, {});

      const values = pipelineKeys.map(_key => {
        let map = {};
        if (!valuesMap[_key]) {
          map = cvsKeys.reduce((prev, cur) => {
            prev[cur] = 0;
            return prev;
          }, {});
        } else {
          const cvsValues = valuesMap[_key].cvsValue.split(',');
          map = cvsKeys.reduce((prev, cur, index) => {
            const _value = +cvsValues[index];
            if (!isNaN(_value)) {
              prev[cur] = _value;
            } else {
              prev[cur] = 0;
            }
            return prev;
          }, {});
        }

        return {
          ...map,
          name: _key
        };
      });

      return {
        ...item,
        values
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
            existingItem.data.push(value[cvsKey] as number);
          } else {
            result.push({
              name: value.name,
              data: [value[cvsKey] as number]
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
