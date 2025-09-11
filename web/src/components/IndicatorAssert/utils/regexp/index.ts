import XRegExp from 'xregexp';
import { i18n} from '@xcan-angus/infra';

const t = i18n.getI18n()?.global?.t || ((v: string) => v);

const match = (regexpStr: string, data: string): string[] | null => {
  if ((regexpStr === '' || regexpStr === undefined || regexpStr === null) ||
    (!['string', 'number'].includes(typeof regexpStr)) ||
    (regexpStr === '' || regexpStr === undefined || regexpStr === null) ||
    (!['string', 'number'].includes(typeof data))) {
    return null;
  }

  /**
   *  Native flags:
   * d - indices for capturing groups (ES2021)
   * g - global
   * i - ignore case
   * m - multiline anchors
   * u - unicode (ES6)
   * y - sticky (Firefox 3+, ES6)
   * Additional XRegExp flags:
   * n - named capture only
   * s - dot matches all (aka singleline) - works even when not natively supported
   * x - free-spacing and line comments (aka extended)
   * A - 21-bit Unicode properties (aka astral) - requires the Unicode Base addon
   */
  // 有效的修饰符
  // const validModifierList = ['d', 'g', 'i', 'm', 'u', 'y'];

  if (typeof regexpStr !== 'string') {
    regexpStr = regexpStr + '';
  }

  if (typeof data !== 'string') {
    data = data + '';
  }

  // 提取修饰符
  const modifier = regexpStr.match(/\/([a-zA-Z]+)$/)?.[1] || '';
  const patternStr = regexpStr.replace(/\/([a-zA-Z]+)$/, '').replace(/^\//g, '').replace(/\/$/g, '');
  try {
    const pattern = XRegExp(patternStr, modifier);
    return data.match(pattern);
  } catch (error) {
    return null;
  }
};

/**
 * @description 校验pattern是否合法
 * @param path 要校验的pattern
 * @returns true - 校验通过，false - 校验不通过
 */
const isValid = (str: string): { valid: boolean; message: string; } => {
  if (str === '' || str === undefined || str === null) {
    return { valid: false, message: t('xcan_apiAssert.utils.regexExpressionEmpty') };
  }

  if (!['string', 'number'].includes(typeof str)) {
    return { valid: false, message: t('xcan_apiAssert.utils.regexExpressionMustBeString') };
  }

  if (typeof str !== 'string') {
    str = JSON.stringify(str);
  }

  try {
    RegExp(str);
    return { valid: true, message: '' };
  } catch (error: any) {
    return { valid: false, message: error?.message };
  }
};

export default { match, isValid };
