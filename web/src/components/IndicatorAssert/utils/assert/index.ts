import { isEqual } from 'lodash-es';
import { i18n, AssertionCondition, AssertionType, utils } from '@xcan-angus/infra';

import expressionUtils from '../expression';
import { Operator } from '../expression/PropsType';
import extract from '../extract';
import proxy, { VariableInfo } from '../proxy';
import { AssertConfig, AssertResult, ConditionResult, Parameter } from './PropsType';

const t = i18n.getI18n()?.global?.t || ((v: string) => v);

const execute = async (data: Parameter, configs: AssertConfig[], variablesInfo:VariableInfo[]): Promise<AssertResult[]> => {
  if (!configs?.length || !data) {
    return [];
  }

  const assertConfigs = JSON.parse(JSON.stringify(configs));
  const assertData = JSON.parse(JSON.stringify(data));

  const safeData = {} as Parameter;
  const _keys = ['form', 'query', 'path', 'header'];
  for (const key in assertData) {
    if (Object.prototype.hasOwnProperty.call(assertData, key) && _keys.includes(key) && !Object.keys(assertData[key]).length) {
      safeData[key] = undefined;
    } else {
      safeData[key] = assertData[key];
    }
  }

  let matchsMap: { [key: string]: [string, string, string] | null } = {};// 条件表达式拆分为[左操作数,运算符,右操作数]的map
  let varMap: {
    [key: string]: {
      failureMessage: string;
      name: string;
      value: string;
    }
  } = {};

  const conditions = assertConfigs.filter(item => item.condition).map(item => item.condition);
  const { extra } = proxy.execute(conditions, variablesInfo);
  matchsMap = extra.matchs;
  varMap = extra.vars;

  const assertResult: AssertResult[] = [];
  for (let i = 0, len = assertConfigs.length; i < len; i++) {
    const {
      assertionCondition,
      condition = '',
      enabled,
      extraction,
      expected = '',
      type,
      parameterName,
      name,
      expression,
      matchItem
    } = assertConfigs[i];
    // 启用的断言才会执行
    if (enabled) {
      let _condition: ConditionResult = {
        failure: false, // 执行结果
        name: '', // 提取的变量名
        conditionMessage: '', // 断言表达式错误的原因
        failureMessage: '', // 提取失败的原因
        value: '', // 提取变量的值
        ignored: false, // 是否忽略该条断言
        message: t('xcan_apiAssert.utils.expressionEmpty')
      };

      let ignored = false;
      if (!condition) {
        _condition = {
          failure: false, // 执行结果
          name: '', // 提取的变量名
          conditionMessage: '', // 断言表达式错误的原因
          failureMessage: '', // 提取失败的原因
          value: '', // 提取变量的值
          ignored: false, // 是否忽略该条断言
          message: t('xcan_apiAssert.utils.expressionEmpty')
        };
      } else {
        const matchs = matchsMap[condition];
        if (!matchs) {
          ignored = true;
          _condition = {
            failure: false, // 执行结果
            name: '', // 提取的变量名
            conditionMessage: t('xcan_apiAssert.utils.expressionFormatError'), // 断言表达式错误的原因
            failureMessage: t('xcan_apiAssert.utils.expressionFormatErrorExtract'), // 提取失败的原因
            value: '', // 提取变量的值
            ignored: true, // 是否忽略该条断言
            message: t('xcan_apiAssert.utils.expressionFormatErrorIgnore')
          };
        } else {
          const [leftOperand, operator, rightOperand] = matchs;
          const _operator = operator as Operator;

          const varValue = varMap[leftOperand];
          let value = '';
          let failureMessage = '';
          if (varValue) {
            value = varValue.value;
            failureMessage = varValue.failureMessage;
          } else {
            failureMessage = t('xcan_apiAssert.utils.variableNotDefined');
            value = leftOperand;
          }

          const flag = expressionUtils.execute(value, _operator, rightOperand);
          if (flag) {
            _condition = {
              failure: false, // 执行结果
              name: leftOperand, // 提取的变量名
              conditionMessage: '', // 断言表达式错误的原因
              failureMessage, // 提取失败的原因
              value, // 提取变量的值
              ignored: false, // 是否忽略该条断言
              message: t('xcan_apiAssert.utils.operationResultSuccess')
            };
          } else {
            ignored = true;
            _condition = {
              failure: true, // 执行结果
              name: leftOperand, // 提取的变量名
              conditionMessage: '', // 断言表达式错误的原因
              failureMessage, // 提取失败的原因
              value, // 提取变量的值
              ignored: true, // 是否忽略该条断言
              message: t('xcan_apiAssert.utils.operationResultFailure')
            };
          }
        }
      }

      // 期望值
      let expectedData: { data: string | null; message: string; errorMessage: string; } = { data: expected, message: '', errorMessage: '' };
      if (extraction) {
        // 提取期望值
        expectedData = extract.execute(extraction, safeData);
        if (expectedData.data !== null) {
          if (typeof expectedData.data === 'object') {
            expectedData.data = JSON.stringify(expectedData.data);
          } else if (typeof expectedData.data !== 'string') {
            expectedData.data = expectedData.data + '';
          }
        }
      }

      const realValueData = getRealvalue({ type, data: safeData, parameterName, assertionCondition, expression, matchItem });
      if (realValueData.data !== null) {
        if (typeof realValueData.data === 'object') {
          realValueData.data = JSON.stringify(realValueData.data);
        } else if (typeof realValueData.data !== 'string') {
          realValueData.data = realValueData.data + '';
        }
      }

      let errorMessage = '';
      if (expectedData?.errorMessage) {
        errorMessage += expectedData?.errorMessage;
      }

      if (ignored) {
        assertResult.push({
          name,
          type,
          parameterName,
          condition,
          assertionCondition,
          extraction: !!extraction,
          _condition,
          result: {
            expectedData,
            realValueData,
            failure: false,
            message: errorMessage
          }
        });
      } else if (errorMessage) {
        assertResult.push({
          name,
          type,
          parameterName,
          condition,
          assertionCondition,
          extraction: !!extraction,
          _condition,
          result: {
            expectedData,
            realValueData,
            failure: true,
            message: errorMessage
          }
        });
      } else {
        const _result = calculate({
          realValueData: realValueData.data,
          assertionCondition: assertionCondition!,
          expectedData: expectedData.data,
          parameterName,
          type,
          extraction
        });
        assertResult.push({
          name,
          type,
          parameterName,
          condition,
          assertionCondition,
          extraction: !!extraction,
          _condition,
          result: { ..._result, realValueData, expectedData }
        });
      }
    }
  }

  return assertResult;
};

const getErrorMessage = (type: AssertionType, name: string): string => {
  if (type === 'BODY') {
    return '响应体';
  }

  return '响应头' + name + '值';
};

const calculate = ({
  realValueData,
  assertionCondition,
  expectedData,
  parameterName,
  type,
  extraction
}: {
  assertionCondition: AssertionCondition;
  realValueData: string | null;
  expectedData: string | null;
  parameterName: string;
  type: AssertionType;
  extraction:{[key:string]:string}|null;
}): { failure: boolean; message: string; } => {
  // 计算结果是否符合预期
  let failure = false;
  let errorMessage = '';
  if (['CONTAIN', 'NOT_CONTAIN'].includes(assertionCondition)) {
    if (utils.isEmpty(realValueData)) {
      // 左值为空，不进行包含、不包含运算
      failure = true;
      errorMessage = getErrorMessage(type, parameterName) + '为空';
    } else if (utils.isEmpty(expectedData)) {
      // 右值为空，不进行包含、不包含运算
      failure = true;
      errorMessage = extraction ? '提取期望值为空' : '期望值为空';
    } else {
      failure = !(assertionCondition === 'CONTAIN' ? realValueData!.includes(expectedData!) : !realValueData!.includes(expectedData!));
    }
  }

  if (['EQUAL', 'NOT_EQUAL'].includes(assertionCondition)) {
    failure = !(assertionCondition === 'EQUAL' ? isEqual(realValueData, expectedData) : !isEqual(realValueData, expectedData));
  }

  if (['GREATER_THAN', 'GREATER_THAN_EQUAL', 'LESS_THAN', 'LESS_THAN_EQUAL'].includes(assertionCondition)) {
    if (utils.isEmpty(realValueData) || utils.isEmpty(expectedData)) {
      failure = true;
      errorMessage = '断言数据为空';
    } else if (isNaN(+realValueData!) || isNaN(+expectedData!)) {
      failure = true;
      errorMessage = '断言数据不是数字类型';
    } else {
      if (assertionCondition === 'GREATER_THAN') {
        failure = !(+realValueData! > +expectedData!);
      } else if (assertionCondition === 'GREATER_THAN_EQUAL') {
        failure = !(+realValueData! >= +expectedData!);
      } else if (assertionCondition === 'LESS_THAN') {
        failure = !(+realValueData! < +expectedData!);
      } else if (assertionCondition === 'LESS_THAN_EQUAL') {
        failure = !(+realValueData! <= +expectedData!);
      }
    }
  }

  if (['IS_EMPTY', 'IS_NULL', 'NOT_EMPTY', 'NOT_NULL'].includes(assertionCondition)) {
    if (assertionCondition === 'IS_EMPTY') {
      failure = realValueData !== '' && realValueData !== null;
    } else if (assertionCondition === 'NOT_EMPTY') {
      failure = realValueData === '' || realValueData === null;
    } else if (assertionCondition === 'IS_NULL') {
      failure = realValueData !== null;
    } else if (assertionCondition === 'NOT_NULL') {
      failure = realValueData === null;
    }
  }

  if (['REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'].includes(assertionCondition)) {
    if (utils.isEmpty(expectedData)) {
      failure = !realValueData;
    } else {
      failure = realValueData !== expectedData;
    }
  }

  return {
    failure,
    message: errorMessage
  };
};

const getRealvalue = (
  {
    type,
    data,
    parameterName,
    expression,
    matchItem,
    assertionCondition
  }:
    {
      type: AssertionType;
      data: Parameter;
      parameterName: string;
      expression: string;
      matchItem: number | undefined;
      assertionCondition: AssertionCondition;
    }): {
      data: string | null;
      message: string;
      errorMessage: string;
    } => {
  if (type === 'BODY') {
    const bodyData = data.responseBody?.data;
    if (assertionCondition === 'REG_MATCH') {
      return extract.regexpExtract({
        data: bodyData,
        matchItem,
        expression,
        defaultValue: ''
      });
    }

    if (assertionCondition === 'XPATH_MATCH') {
      return extract.xpathExtract({
        data: bodyData,
        matchItem,
        expression,
        defaultValue: ''
      });
    }

    if (assertionCondition === 'JSON_PATH_MATCH') {
      return extract.jsonpathExtract({
        data: bodyData,
        matchItem,
        expression,
        defaultValue: ''
      });
    }

    return {
      data: bodyData === null ? null : JSON.stringify(bodyData),
      message: '',
      errorMessage: ''
    };
  }

  if (type === 'BODY_SIZE') {
    return {
      data: data.responseBody?.size + '',
      message: '',
      errorMessage: ''
    };
  }

  if (type === 'DURATION') {
    return {
      data: data.duration + '',
      message: '',
      errorMessage: ''
    };
  }

  if (type === 'HEADER') {
    // 响应头、请求头的名称对大小写不敏感，全部转为小写
    const lowerName = parameterName?.toLowerCase();
    if (data?.responseHeader) {
      const keys = Object.keys(data.responseHeader);
      for (let i = 0, len = keys.length; i < len; i++) {
        if (keys[i].toLowerCase() === lowerName) {
          if (assertionCondition === 'REG_MATCH') {
            return extract.regexpExtract({
              data: data.responseHeader[keys[i]],
              matchItem,
              expression,
              defaultValue: ''
            });
          } else if (assertionCondition === 'XPATH_MATCH') {
            return extract.xpathExtract({
              data: data.responseHeader[keys[i]],
              matchItem,
              expression,
              defaultValue: ''
            });
          } else {
            return {
              data: data.responseHeader[keys[i]],
              message: '',
              errorMessage: ''
            };
          }
        }
      }
    }

    return {
      data: null,
      message: t('xcan_indicatorAssert.utils.responseHeaderNameNotExist'),
      errorMessage: t('xcan_indicatorAssert.utils.responseHeaderNameNotExist')
    };
  }

  if (type === 'SIZE') {
    const headerSize = new TextEncoder().encode(JSON.stringify(data.responseHeader)).length;
    const bodySize = data.responseBody?.size;
    return {
      data: headerSize + bodySize + '',
      message: t('xcan_indicatorAssert.utils.responseHeaderNameNotExist'),
      errorMessage: t('xcan_indicatorAssert.utils.responseHeaderNameNotExist')
    };
  }

  return {
    data: data.status + '',
    message: t('xcan_indicatorAssert.utils.responseHeaderNameNotExist'),
    errorMessage: t('xcan_indicatorAssert.utils.responseHeaderNameNotExist')
  };
};

export default { execute };
