import { select as xselect } from 'xpath-next/dist/xpath';
import { DOMParser } from 'xmldom';

const select = (xml: string, path: string): string[] => {
  if (typeof xml !== 'string' ||
  (path === '' || path === undefined || path === null) ||
  (!['string', 'number'].includes(typeof path))) {
    return [];
  }

  if (typeof path !== 'string') {
    path = path + '';
  }

  try {
    const doc = new DOMParser().parseFromString(xml);
    const nodes = xselect(path, doc, false) as { textContent: string }[];
    return nodes?.reduce((prev, cur) => {
      const data = cur.textContent.replace(/\r*\n\s*/gms, '');
      prev.push(data);
      return prev;
    }, [] as string[]) || [];
  } catch (error) {
    return [];
  }
};

/**
 * @description 校验xpath是否合法
 * @param path 要校验的xpath
 * @returns true - 校验通过，false - 校验不通过
 */
const isValid = (path: string): { valid: boolean; message: string; } => {
  if (path === '' || path === undefined || path === null) {
    return { valid: false, message: 'XPath表达式不能为空' };
  }

  if (!['string', 'number'].includes(typeof path)) {
    return { valid: false, message: 'XPath表达式必须是string类型' };
  }

  if (typeof path !== 'string') {
    path = path + '';
  }

  try {
    const doc = new DOMParser().parseFromString('<root></root>', 'text/xml');
    xselect(path, doc, false);
    return { valid: true, message: '' };
  } catch (error: any) {
    return { valid: false, message: error?.message };
  }
};

export default { select, isValid };
