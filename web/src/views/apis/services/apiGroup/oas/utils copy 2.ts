import { DataType, OpenApiInfo } from './PropsType';

const getComponentItem = (refStr: string, openapiComponents: OpenApiInfo['components']) => {
  if (!refStr || !openapiComponents) {
    return {};
  }

  const fields = refStr.split('/').slice(2);
  let item = {};
  if (fields.length > 0) {
    item = fields.reduce((prev, cur) => {
      prev = prev[cur];
      return prev || {};
    }, openapiComponents);
  }

  return item;
};

const getArraySchema = (data: { [key: string]: any }[], openapiComponents: OpenApiInfo['components']) => {
  if (!data?.length) {
    return [];
  }

  const list = [];
  for (let i = 0, len = data.length; i < len; i++) {
    const item = data[i];
    if (Object.prototype.toString.call(item) === '[object Object]') {
      list.push(getObjectShcema(item, openapiComponents));
    } else if (Array.isArray(item)) {
      list.push(...getArraySchema(item, openapiComponents));
    } else {
      list.push(item);
    }
  }

  return list;
};

const getObjectShcema = (data: { [key: string]: any }, openapiComponents: OpenApiInfo['components']) => {
  if (!data) {
    return {};
  }

  let map = {};
  for (const key in data) {
    const item = data[key];
    if (key === '$ref') {
      let shcemaItem = getComponentItem(item, openapiComponents);
      if (Object.prototype.toString.call(shcemaItem === '[object Object]')) {
        shcemaItem = getObjectShcema(shcemaItem, openapiComponents);
        map = {
          ...data,
          ...map,
          ...shcemaItem
        };
      }
      delete map[key];
    } else if (Object.prototype.toString.call(item) === '[object Object]') {
      map[key] = getObjectShcema(item, openapiComponents);
    } else if (Array.isArray(item)) {
      map[key] = getArraySchema(item, openapiComponents);
    } else {
      map[key] = item;
    }
  }

  return map;
};

const stringify = (data: any) => {
  if (typeof data === 'object') {
    return JSON.stringify(data);
  }

  if (typeof data !== 'string') {
    return data + '';
  }

  return data;
};

const getSchemeExample = (data: {
  examples?: { [x: string]: { value: any; }; };
  example?: any;
  schema: {
    type: DataType;
    examples?: any[];
    example?: any;
    enum?: string[];
  };
}) => {
  if (Object.prototype.hasOwnProperty.call(data, 'examples')) {
    if (Object.prototype.toString.call(data.examples) === '[object Object]') {
      for (const key in data.examples) {
        return stringify(data.examples[key]?.value);
      }
    }
  }

  if (Object.prototype.hasOwnProperty.call(data, 'example')) {
    return stringify(data.example);
  }

  if (Object.prototype.hasOwnProperty.call(data, 'x-xc-value')) {
    return stringify(data['x-xc-value']);
  }

  const schema = data.schema;
  if (schema) {
    if (Object.prototype.hasOwnProperty.call(schema, 'examples')) {
      return stringify(schema.examples?.[0]);
    }

    if (Object.prototype.hasOwnProperty.call(schema, 'example')) {
      return stringify(schema.example);
    }

    if (Object.prototype.hasOwnProperty.call(schema, 'x-xc-value')) {
      return stringify(schema['x-xc-value']);
    }

    if (schema.type === 'string' && schema.enum?.length) {
      return schema.enum[0];
    }
  }

  return undefined;
};

const getPropertyExample = (data: {
  type: DataType;
  examples?: { [x: string]: { value: any; }; };
  example?: any;
  enum?: string[];
}) => {
  if (Object.prototype.hasOwnProperty.call(data, 'examples')) {
    if (Array.isArray(data.examples)) {
      return stringify(data.examples[0]);
    }

    return stringify(data.examples);
  }

  if (Object.prototype.hasOwnProperty.call(data, 'example')) {
    return stringify(data.example);
  }

  if (Object.prototype.hasOwnProperty.call(data, 'x-xc-value')) {
    return stringify(data['x-xc-value']);
  }

  if (data.type === 'string' && data.enum?.length) {
    return data.enum[0];
  }

  return undefined;
};

const encode = (data, encodeFlag = true):string => {
  let str = '';
  if (Array.isArray(data)) {
    for (let i = 0, len = data.length; i < len; i++) {
      const item = data[i];
      if (item !== null && item !== undefined && item !== '') {
        if (encodeFlag) {
          if (typeof data === 'object' && data !== null) {
            str += encodeURIComponent(JSON.stringify(item)) + ',';
          } else {
            str += encodeURIComponent(item) + ',';
          }
        } else {
          if (typeof data === 'object' && data !== null) {
            str += JSON.stringify(item) + ',';
          } else {
            str += item + ',';
          }
        }
      }
    }
  } else if (typeof data === 'object' && data !== null) {
    for (const key in data) {
      const item = data[key];
      if (item !== null && item !== undefined && item !== '') {
        if (encodeFlag) {
          str += key + '=' + encodeURIComponent(JSON.stringify(item)) + ',';
        } else {
          str += key + '=' + JSON.stringify(item) + ',';
        }
      }
    }
  } else {
    if (data !== null && data !== undefined && data !== '') {
      if (encodeFlag) {
        str = encodeURIComponent(data);
      } else {
        str = data + '';
      }
    }
  }

  return str.replace(/,$/, '');
};

export default { encode, stringify, getSchemeExample, getPropertyExample, getComponentItem, getArraySchema, getObjectShcema };
export { encode, stringify, getSchemeExample, getPropertyExample, getComponentItem, getArraySchema, getObjectShcema };
