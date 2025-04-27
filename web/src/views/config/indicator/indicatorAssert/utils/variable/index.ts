import { http, TESTER } from '@xcan-angus/tools';

import { VariableItem } from './PropsType';

const sorts = ['CURRENT-API', 'CURRENT-SERVICE', 'CURRENT-PROJECT', 'GLOBAL-API', 'GLOBAL-SERVICE', 'GLOBAL-PROJECT'];
const enable = (variables: VariableItem[]): VariableItem | undefined => {
  const map: { [key: string]: VariableItem } = {};
  for (let i = 0, len = variables.length; i < len; i++) {
    if (variables[i].enabled) {
      map[variables[i].scope.value + '-' + variables[i].targetType.value] = variables[i];
    }
  }

  for (let i = 0, len = sorts.length; i < len; i++) {
    if (map[sorts[i]]) {
      return map[sorts[i]];
    }
  }
};

/**
 * @description 查询变量详情
 * @param variable
 * @returns
 */
const load = async (id:string, names:string[]):Promise<null|{
  extractFlag: boolean;
  failureMessage: string;
  name: string;
  value: string;
}[]> => {
  // const [error, { data }] = await http.getVariables({ names, targetId: id, targetType: 'API' });
  const [error, { data }] = await http.get(`${TESTER}/variable/value/all`, { names, targetId: id, targetType: 'API' });
  if (error) {
    return null;
  }

  return data;
};

export default { enable, load };
