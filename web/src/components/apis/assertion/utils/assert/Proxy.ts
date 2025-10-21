import expression from '../expression/Expression';

export type VariableInfo = {name: string;value: string;failureMessage:string;};

export type MatchInfo = {[key: string]:[string, string, string]|null;}

const execute = (conditions: string[], variablesInfo:VariableInfo[]): {data:{[key:string]:string;};extra:{vars:{[key:string]:VariableInfo};matchs:MatchInfo;}} => {
  const matchsMap: { [key: string]: [string, string, string] | null } = {};// 条件表达式拆分为[左操作数,运算符,右操作数]的map
  const varMap:{[key:string]:string} = {};// 变量=>值
  const varInfoMap:{[key:string]:VariableInfo} = {} as {[key:string]:VariableInfo};// 查询到的变量信息

  const vars: string[] = [];
  for (let i = 0, len = conditions.length; i < len; i++) {
    const matchs = expression.split(conditions[i]);
    matchsMap[conditions[i]] = matchs;
    if (matchs?.length && !vars.includes(matchs[0])) {
      vars.push(matchs[0]);
    }
  }

  if (!vars.length) {
    return {
      data: varMap,
      extra: {
        vars: varInfoMap,
        matchs: matchsMap
      }
    };
  }

  // 查询变量接口，该接口会返回变量替换值，如果没有查询到该变量则把该变量当成值进行运算
  const dataMap:VariableInfo = variablesInfo?.reduce((prev, cur) => {
    prev[cur.name] = cur;
    return prev;
  }, {} as VariableInfo) || ({} as VariableInfo);

  for (let i = 0, len = vars.length; i < len; i++) {
    const key = vars[i];
    varInfoMap[key] = dataMap[key];
    if (dataMap[key]) {
      varMap[key] = dataMap[key]?.value || key;
    }
  }

  return {
    data: varMap,
    extra: {
      vars: varInfoMap,
      matchs: matchsMap
    }
  };
};

export default { execute };
