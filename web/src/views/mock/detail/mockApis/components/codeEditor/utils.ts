import YAMl from 'yaml';

const isJSON = (str: string): boolean => {
  if (typeof str !== 'string') {
    return false;
  }

  try {
    const obj = JSON.parse(str);
    return typeof obj === 'object';
  } catch (e) {
    return false;
  }
};

const isHtml = (str:string):boolean => {
  if (typeof str !== 'string') {
    return false;
  }

  const doc = new DOMParser().parseFromString(str, 'text/html');
  return Array.from(doc.body.childNodes).some(node => node.nodeType === 1);
};

const isXML = (str: string): boolean => {
  if (typeof str !== 'string') {
    return false;
  }

  const parser = new DOMParser();
  const doc = parser.parseFromString(str, 'text/xml');
  const errors = doc.getElementsByTagName('parsererror');

  return errors.length === 0;
};

const isYAML = (str: string): boolean => {
  if (typeof str !== 'string') {
    return false;
  }

  try {
    const obj = YAMl.parse(str);
    return typeof obj === 'object';
  } catch (e) {
    return false;
  }
};

export { isJSON, isHtml, isXML, isYAML };
