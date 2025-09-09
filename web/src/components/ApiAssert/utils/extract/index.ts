import { utils, ExtractionMethod } from '@xcan-angus/infra';
import { i18n} from '@xcan-angus/infra';

const t = i18n.getI18n()?.global?.t || ((v: string) => v);

import jsonpath from '../jsonpath';
import xpath from '../xpath';
import regexp from '../regexp';
import { Parameter } from '../assert/PropsType';

/**
 * @description 提取期望值
 * @param extraction 提取配置信息
 * @param config 接口参数
 * @returns { data:提取期望值, errorMessage:错误信息 }
 */
const execute = (extraction: {
  location: string | undefined;
  method: ExtractionMethod | undefined;
  parameterName?: string;
  defaultValue?: string;
  expression?: string;
  apisId?: string;
  matchItem?: number;// 提取匹配第几项
}, config?: Parameter): { data: string | null; errorMessage: string; message: string } => {
  const location = extraction.location;
  const method = extraction.method;
  const defaultValue = (utils.isEmpty(extraction.defaultValue) ? '' : extraction.defaultValue) as string;
  const parameterName = extraction.parameterName;
  const matchItem: number | undefined = extraction.matchItem;
  if (!location) {
    return {
      data: defaultValue,
      message: defaultValue ? t('xcan_apiAssert.extractionLocationMissingWithDefault') : t('xcan_apiAssert.extractionLocationMissing'),
      errorMessage: t('xcan_apiAssert.extractionLocationMissing')
    };
  }

  if (!method) {
    return {
      data: defaultValue,
      message: defaultValue ? t('xcan_apiAssert.extractionMethodMissingWithDefault') : t('xcan_apiAssert.extractionMethodMissing'),
      errorMessage: t('xcan_apiAssert.extractionMethodMissing')
    };
  }

  // form参数、path参数、请求参数、请求头、响应头参数名称必须，没有参数返回默认值；响应体、raw请求体禁用提取参数名称
  if (['FORM_PARAMETER', 'PATH_PARAMETER', 'QUERY_PARAMETER', 'REQUEST_HEADER', 'RESPONSE_HEADER'].includes(location)) {
    if (!parameterName) {
      return {
        data: defaultValue,
              message: defaultValue ? t('xcan_apiAssert.extractionParameterNameMissingWithDefault') : t('xcan_apiAssert.extractionParameterNameMissing'),
      errorMessage: t('xcan_apiAssert.extractionParameterNameMissing')
      };
    }
  }

  let originData: any;
  if (location === 'FORM_PARAMETER') {
    if (!config?.form) {
      return {
        data: defaultValue,
        message: defaultValue ? t('xcan_apiAssert.formParameterEmptyWithDefault') : t('xcan_apiAssert.formParameterEmpty'),
        errorMessage: t('xcan_apiAssert.formParameterEmpty')
      };
    }

    if (!Object.prototype.hasOwnProperty.call(config.form, parameterName!)) {
      return {
        data: defaultValue,
        message: defaultValue ? t('xcan_apiAssert.formParameterMissingWithDefault', { parameterName }) : t('xcan_apiAssert.formParameterMissing', { parameterName }),
        errorMessage: t('xcan_apiAssert.formParameterMissing', { parameterName })
      };
    }

    originData = config.form[parameterName!];
  } else if (location === 'PATH_PARAMETER') {
    if (!config?.path) {
      return {
        data: defaultValue,
        message: defaultValue ? t('xcan_apiAssert.pathParameterEmptyWithDefault') : t('xcan_apiAssert.pathParameterEmpty'),
        errorMessage: t('xcan_apiAssert.pathParameterEmpty')
      };
    }

    if (!Object.prototype.hasOwnProperty.call(config.path, parameterName!)) {
      return {
        data: defaultValue,
        message: defaultValue ? t('xcan_apiAssert.pathParameterMissingWithDefault', { parameterName }) : t('xcan_apiAssert.pathParameterMissing', { parameterName }),
        errorMessage: t('xcan_apiAssert.pathParameterMissing', { parameterName })
      };
    }

    originData = config.path[parameterName!];
  } else if (location === 'QUERY_PARAMETER') {
    if (!config?.query) {
      return {
        data: defaultValue,
              message: defaultValue ? t('xcan_apiAssert.queryParameterEmptyWithDefault') : t('xcan_apiAssert.queryParameterEmpty'),
      errorMessage: t('xcan_apiAssert.queryParameterEmpty')
      };
    }

    if (!Object.prototype.hasOwnProperty.call(config.query, parameterName!)) {
      return {
        data: defaultValue,
              message: defaultValue ? t('xcan_apiAssert.queryParameterMissingWithDefault', { parameterName }) : t('xcan_apiAssert.queryParameterMissing', { parameterName }),
      errorMessage: t('xcan_apiAssert.queryParameterMissing', { parameterName })
      };
    }

    originData = config.query[parameterName!];
  } else if (location === 'REQUEST_HEADER') {
    if (!config?.header) {
      return {
        data: defaultValue,
              message: defaultValue ? t('xcan_apiAssert.requestHeaderEmptyWithDefault') : t('xcan_apiAssert.requestHeaderEmpty'),
      errorMessage: t('xcan_apiAssert.requestHeaderEmpty')
      };
    }

    if (!Object.prototype.hasOwnProperty.call(config.header, parameterName!)) {
      return {
        data: defaultValue,
              message: defaultValue ? t('xcan_apiAssert.requestHeaderMissingWithDefault', { parameterName }) : t('xcan_apiAssert.requestHeaderMissing', { parameterName }),
      errorMessage: t('xcan_apiAssert.requestHeaderMissing', { parameterName })
      };
    }

    const lowerName = parameterName?.toLowerCase();
    const keys = Object.keys(config.header);
    for (let i = 0, len = keys.length; i < len; i++) {
      if (keys[i].toLowerCase() === lowerName) {
        originData = config.header[keys[i]];
        break;
      }
    }
  } else if (location === 'RESPONSE_BODY') {
    if (!config?.responseBody?.data) {
      return {
        data: defaultValue,
              message: defaultValue ? t('xcan_apiAssert.responseBodyEmptyWithDefault') : t('xcan_apiAssert.responseBodyEmpty'),
      errorMessage: t('xcan_apiAssert.responseBodyEmpty')
      };
    }

    // 从整个响应体进行提取
    originData = config.responseBody.data;
  } else if (location === 'RESPONSE_HEADER') {
    if (!config?.responseHeader) {
      return {
        data: defaultValue,
              message: defaultValue ? t('xcan_apiAssert.responseHeaderEmptyWithDefault') : t('xcan_apiAssert.responseHeaderEmpty'),
      errorMessage: t('xcan_apiAssert.responseHeaderEmpty')
      };
    }

    if (!Object.prototype.hasOwnProperty.call(config.responseHeader, parameterName!)) {
      return {
        data: defaultValue,
              message: defaultValue ? t('xcan_apiAssert.responseHeaderMissingWithDefault', { parameterName }) : t('xcan_apiAssert.responseHeaderMissing', { parameterName }),
      errorMessage: t('xcan_apiAssert.responseHeaderMissing', { parameterName })
      };
    }

    const lowerName = parameterName?.toLowerCase();
    const keys = Object.keys(config.responseHeader);
    for (let i = 0, len = keys.length; i < len; i++) {
      if (keys[i].toLowerCase() === lowerName) {
        originData = config.responseHeader[keys[i]];
        break;
      }
    }
  } else if (location === 'REQUEST_RAW_BODY') {
    if (!config?.rawBody) {
      return {
        data: defaultValue,
              message: defaultValue ? t('xcan_apiAssert.rawRequestBodyEmptyWithDefault') : t('xcan_apiAssert.rawRequestBodyEmpty'),
      errorMessage: t('xcan_apiAssert.rawRequestBodyEmpty')
      };
    }

    // 从整个raw请求体进行提取
    originData = config.rawBody;
  }

  if (utils.isEmpty(originData)) {
    return {
      data: defaultValue,
      message: '',
      errorMessage: ''
    };
  }

  if (method === 'EXACT_VALUE') {
    if (typeof originData !== 'string') {
      originData = JSON.stringify(originData);
    }

    return {
      data: originData || defaultValue,
      message: '',
      errorMessage: ''
    };
  }

  if (!extraction.expression) {
    return {
      data: defaultValue,
      message: defaultValue ? t('xcan_apiAssert.utils.extractionExpressionMissingWithDefault') : t('xcan_apiAssert.utils.extractionExpressionMissing'),
      errorMessage: t('xcan_apiAssert.utils.extractionExpressionMissing')
    };
  }

  const extractData = { data: originData, defaultValue, matchItem, expression: extraction.expression };

  if (method === 'REGEX') {
    return regexpExtract(extractData);
  }

  if (method === 'X_PATH') {
    return xpathExtract(extractData);
  }

  return jsonpathExtract(extractData);
};

const regexpExtract = ({
  data,
  defaultValue,
  matchItem,
  expression
}: { data: any, defaultValue: string, matchItem: number | undefined, expression: string }): { data: string | null; message: string; errorMessage: string; } => {
  try {
    let _data: string;
    if (typeof data !== 'string') {
      _data = JSON.stringify(data);
    } else {
      _data = data;
    }

    const result = regexp.match(expression, _data);
    let targetData: string | null = defaultValue;

    if (matchItem && typeof matchItem === 'string') {
      matchItem = +matchItem;
      if (isNaN(matchItem)) {
        matchItem = undefined;
      }
    }

    if (result) {
      if (typeof matchItem === 'number') {
        targetData = result[matchItem];
      } else {
        targetData = result[0];
      }
    }

    if (utils.isEmpty(targetData)) {
      if (defaultValue) {
        targetData = defaultValue;
      } else if (targetData === undefined) {
        targetData = '';
      }
    } else if (typeof targetData !== 'string') {
      targetData = JSON.stringify(targetData);
    }

    return {
      data: targetData,
      message: result ? '' : (defaultValue ? t('xcan_apiAssert.utils.extractedValueEmptyWithDefault') : t('xcan_apiAssert.utils.extractedValueEmpty')),
      errorMessage: ''
    };
  } catch (error) {
    return {
      data: defaultValue,
      message: defaultValue ? t('xcan_apiAssert.utils.regexExpressionErrorWithDefault') : t('xcan_apiAssert.utils.regexExpressionError'),
      errorMessage: t('xcan_apiAssert.utils.regexExpressionError')
    };
  }
};

const xpathExtract = ({
  data,
  defaultValue,
  matchItem,
  expression
}: { data: any, defaultValue: string, matchItem: number | undefined, expression: string }): { data: string | null; message: string; errorMessage: string; } => {
  try {
    let _data: string;
    if (typeof data !== 'string') {
      _data = JSON.stringify(data);
    } else {
      _data = data;
    }

    const result = xpath.select(_data, expression);
    const len = result.length;
    let targetData: string | string[] | null = defaultValue;

    if (matchItem && typeof matchItem === 'string') {
      matchItem = +matchItem;
      if (isNaN(matchItem)) {
        matchItem = undefined;
      }
    }

    if (len > 0) {
      if (typeof matchItem === 'number') {
        targetData = result[matchItem];
      } else {
        if (len === 1) {
          targetData = result[0];
        } else {
          targetData = result;
        }
      }
    }

    if (utils.isEmpty(targetData)) {
      if (defaultValue) {
        targetData = defaultValue;
      } else if (targetData === undefined) {
        targetData = '';
      }
    } else if (typeof targetData !== 'string') {
      if (Array.isArray(targetData)) {
        targetData = targetData.join('');
      } else {
        targetData = JSON.stringify(targetData);
      }
    }

    return {
      data: targetData as string,
      message: result ? '' : (defaultValue ? t('xcan_apiAssert.utils.extractedValueEmptyWithDefault') : t('xcan_apiAssert.utils.extractedValueEmpty')),
      errorMessage: ''
    };
  } catch (error) {
    return {
      data: defaultValue,
      message: defaultValue ? t('xcan_apiAssert.utils.xpathExpressionErrorWithDefault') : t('xcan_apiAssert.utils.xpathExpressionError'),
      errorMessage: t('xcan_apiAssert.utils.xpathExpressionError')
    };
  }
};

const jsonpathExtract = ({
  data,
  defaultValue,
  matchItem,
  expression
}: { data: any, defaultValue: string, matchItem: number | undefined, expression: string }): { data: string | null; message: string; errorMessage: string; } => {
  try {
    const jsonpathOption = { path: expression, json: data };
    const result = jsonpath.JSONPath(jsonpathOption);
    const len = result.length;
    let targetData: string | string[] | null = defaultValue;

    if (matchItem && typeof matchItem === 'string') {
      matchItem = +matchItem;
      if (isNaN(matchItem)) {
        matchItem = undefined;
      }
    }

    if (len > 0) {
      if (typeof matchItem === 'number') {
        targetData = result[matchItem];
      } else {
        if (len === 1) {
          targetData = result[0];
        } else {
          targetData = result;
        }
      }
    }

    if (utils.isEmpty(targetData)) {
      if (defaultValue) {
        targetData = defaultValue;
      } else if (targetData === undefined) {
        targetData = '';
      }
    } else if (typeof targetData !== 'string') {
      targetData = JSON.stringify(targetData);
    }

    return {
      data: targetData as string,
      message: len ? '' : (defaultValue ? t('xcan_apiAssert.utils.extractedValueEmptyWithDefault') : t('xcan_apiAssert.utils.extractedValueEmpty')),
      errorMessage: ''
    };
  } catch (error) {
    return {
      data: defaultValue,
      message: defaultValue ? t('xcan_apiAssert.utils.jsonpathExpressionErrorWithDefault') : t('xcan_apiAssert.utils.jsonpathExpressionError'),
      errorMessage: t('xcan_apiAssert.utils.jsonpathExpressionError')
    };
  }
};

export default { execute, regexpExtract, xpathExtract, jsonpathExtract };
