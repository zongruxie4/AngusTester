import { Operator, Typeof } from './types';

export const COMPARISON_OPERATORS: readonly Operator[] = ['=', '!=', '>=', '<=', '>', '<'];
export const TYPES: readonly Typeof[] = ['boolean', 'number', 'string', 'undefined'];

/**
 * @description 提取变量，不满足变量规则，则返回输入值。变量的格式为${变量名}
 * @param str 要提取变量的字符串
 * @returns 提取后的变量值或输入值
 */
const extractVar = (str: string): string => {
  const regex = /^\{([^}][a-zA-Z0-9!@$%^&*()_\-+=./]+)\}$/;
  const match = str.match(regex);
  return match ? match[1] : str;
};

/**
 * @description 把字符串根据指定运算符拆分为[左操作数,运算符,右操作数]
 * @param str 要拆分的字符
 * @returns [左操作数,运算符,右操作数] 或 undefined
 */
const split = (condition: string): [string, string, string] | null => {
  if (!condition) {
    return null;
  }

  const COMPARISON_OPERATORS: readonly Operator[] = ['=', '!=', '>=', '<=', '>', '<'];
  const operatorRegex = new RegExp(`^([^\\s${COMPARISON_OPERATORS.join('')}]+)\\s*(${COMPARISON_OPERATORS.join('|')})\\s*([^${COMPARISON_OPERATORS.join('')}]+)$`);
  const match = condition.trim().match(operatorRegex);
  if (match && match.length === 4) {
    const varStr = extractVar(match[1].trim());
    return [varStr, match[2].trim(), match[3].trim()];
  } else {
    // 输入字符串非法，包含多个运算符或不包含运算符或左变量和右边值包含指定的运算符
    return null;
  }
};

/**
 * @description 校验条件表达式格式
 * @param condition 条件表达式
 * @returns true - 条件表达式格式正确，false - 条件表达式格式错误
 */
const isValid = (condition: string): boolean => {
  return !!split(condition);
};

const isAllDigits = (str: string): boolean => {
  return /^\d+$/.test(str);
};

/**
 * @description 只支持基本类型
 * @param value 校验的数据
 * @returns true - 校验通过，false - 校验不通过
 */
const checkType = (value: any): boolean => {
  // eslint-disable-next-line @typescript-eslint/ban-ts-comment
  // @ts-ignore
  return TYPES.includes(typeof value);
};

/**
 * @description 不用恒等，例如：'1' == 1 结果为true，'true'==true 结果为true，'false'==false 结果为true
 * @param leftOperand 左操作数，基本数据类型
 * @param operator 运算符
 * @param rightOperand 右操作数，基本数据类型
 * @returns boolean true - 执行表达式通过，false - 执行表达式不通过
 */
const execute = (leftOperand: string | undefined, operator: Operator, rightOperand: string | undefined): boolean => {
  // 不在运算符集合内不进行运算
  if (!COMPARISON_OPERATORS.includes(operator)) {
    return false;
  }

  // 只支持基本数据类型运算。 typeof null === 'object'
  if (leftOperand !== null && leftOperand !== null) {
    if (!checkType(leftOperand) || !checkType(leftOperand)) {
      return false;
    }
  }

  const leftType = typeof leftOperand;
  const rightType = typeof rightOperand;
  // 数据类型不一致，则自动转换类型，只有string 可以和其他类型互转
  if (leftType !== rightType && (leftType !== 'string' && rightType !== 'string')) {
    return false;
  }

  // 统一转为string
  const left = leftOperand + '';
  const right = rightOperand + '';

  if (operator === '=') {
    return left === right;
  }

  if (operator === '!=') {
    return left !== right;
  }

  // '>=', '<=', '>', '<' 运算符的数据类型必须是数字
  if (!isAllDigits(left) || !isAllDigits(right)) {
    return false;
  }

  if (operator === '<') {
    return +left < +right;
  }

  if (operator === '<=') {
    return +left <= +right;
  }

  if (operator === '>') {
    return +left > +right;
  }
  if (operator === '>=') {
    return +left >= +right;
  }

  return false;
};

export default { execute, split, isValid };
