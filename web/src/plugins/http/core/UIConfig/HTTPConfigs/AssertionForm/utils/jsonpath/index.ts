import { JSONPath as jp, JSONPathOptions } from 'jsonpath-plus';

const JSONPath = (option: JSONPathOptions): string[] => {
  if ((option.path === '' || option.path === undefined || option.path === null) ||
    (!['string', 'number'].includes(typeof option.path)) ||
    (option.json === '' || option.json === undefined || option.json === null) ||
    (!['string', 'object'].includes(typeof option.path))
  ) {
    return [];
  }

  try {
    let jsonData = option.json;
    if (typeof jsonData === 'string') {
      jsonData = JSON.parse(jsonData);
    }

    return jp({ ...option, json: jsonData });
  } catch (error) {
    return [];
  }
};

/**
 * @description 校验jsonpath是否合法
 * @param path 要校验的jsonpath
 * @returns true - 校验通过，false - 校验不通过
 */
const isValid = (path: string): { valid: boolean; message: string; } => {
  if (path === '' || path === undefined || path === null) {
    return { valid: false, message: 'JSONPath表达式不能为空' };
  }

  if (!['string', 'number'].includes(typeof path)) {
    return { valid: false, message: 'JSONPath表达式必须是string类型' };
  }

  return { valid: true, message: '' };
};

export default { JSONPath, isValid };
