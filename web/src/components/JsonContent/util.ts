import { utils } from '@xcan-angus/infra';
import { API_EXTENSION_KEY, getDataTypeFromFormat } from '@/utils/apis/index';

const { valueKey, enabledKey } = API_EXTENSION_KEY;

export const itemTypes = [
  'string',
  'array',
  'boolean',
  'integer',
  'object',
  'number'
].map(i => ({ value: i, label: i }));

export const inOptions = ['path', 'query'].map(i => ({ value: i, label: i }));

export const baseTypes = [
  'string',
  'boolean',
  'integer',
  'number'
].map(i => ({ value: i, label: i }));

export const parameterIn = [
  'query',
  'path'
].map(i => ({ value: i, label: i }));

export const getDefaultItem = (config = {}) => {
  return {
    name: '',
    id: utils.uuid('api'),
    [valueKey]: '',
    type: 'string',
    [enabledKey]: true,
    ...config
  };
};

const getDataType = (data) => {
  const type = typeof data;
  if (type !== 'object') {
    return type;
  }
  if (Object.prototype.toString.call(data) === '[object Array]') {
    return 'array';
  }
  if (Object.prototype.toString.call(data) === '[object Object]') {
    return 'object';
  }
};

/**
 *transform JSON to list<key, value>[]
 *
 */
export const transJsonToList = (data: any [] | Record<string, any>, pid = -1, level = 1, defaultData: any[] = [], schema = {}, topSchema = {}): any[] => {
  const transArr = (data, pid = -1, level = 1, schema) => {
    data.forEach(i => {
      let copySchema = JSON.parse(JSON.stringify((schema || {})));
      if (['object', 'array'].includes(copySchema.type) || getDataType(i) === 'object' || getDataType(i) === 'array') {
        const id = utils.uuid('api');
        const idLine = [...(result.find(item => item.id === pid)?.idLine || []), id];
        result.push({
          type: getDataType(i),
          ...copySchema,
          name: 'item',
          id,
          pid,
          idLine,
          level: level,
          [valueKey]: undefined,
          checked: true
        });
        level += 1;
        pid = id;
        copySchema = copySchema.items || {};
        transJsonToList(i, pid, level, result, copySchema.items || {});
      } else {
        const id = utils.uuid('api');
        const idLine = [...(result.find(item => item.id === pid)?.idLine || []), id];
        result.push({
          type: getDataType(i),
          ...copySchema,
          name: 'item',
          id,
          pid,
          idLine,
          level: level,
          [valueKey]: i,
          checked: true
        });
      }
    });
  };
  const transObjct = (data, pid = -1, level = 1, schema) => {
    Object.keys(data).forEach(key => {
      let value = data[key];
      let type = schema[key]?.type || getDataType(value);
      if (!schema[key]?.type && schema[key]?.format) {
        type = getDataTypeFromFormat(schema[key].format);
      }
      const id = utils.uuid('api');
      const hasChild = type === 'array' || type === 'object';
      const idLine = [...(result.find(item => item.id === pid)?.idLine || []), id];
      result.push({
        type: type,
        ...schema[key],
        name: key,
        [valueKey]: value,
        pid: pid,
        level,
        idLine,
        id,
        checked: true
      });
      if (hasChild) {
        if (type === 'array') {
          if (typeof value === 'string') {
            try {
              value = JSON.parse(value);
            } catch {
              console.log(value + 'id not a json');
            }
          }
          transArr(value, id, level + 1, schema.properties?.[key] || {});
        }
        if (type === 'object') {
          if (typeof value === 'string') {
            try {
              value = JSON.parse(value);
            } catch {
              console.log(value + 'id not a json');
            }
          }
          transObjct(value, id, level + 1, schema.properties?.[key] || {});
        }
      }
    });
  };

  const transNormal = (data, pid = -1, level = 1, _schema) => {
    const id = utils.uuid('api');
    const pItem = result.find(item => item.id === pid) || { type: topSchema?.type };
    const idLine = [...(pItem?.idLine || []), id];
    let type = _schema.type || getDataType(data);
    if (!_schema.type && _schema.format) {
      type = getDataTypeFromFormat(_schema.format);
    }
    return result.push({
      type,
      name: pItem?.type === 'array' ? 'item' : '',
      ..._schema,
      [valueKey]: data,
      id,
      pid,
      level,
      idLine,
      checked: true
    });
  };
  const result:any[] = defaultData;
  const type = schema.type || getDataType(data);
  if (type !== 'object' && type !== 'array') {
    transNormal(data, pid, level, schema || {});
  }
  if (typeof data === 'string') {
    try {
      data = JSON.parse(data);
    } catch {
      console.log(data + 'id not a json');
    }
  }
  if (type === 'array') {
    transArr(data, pid, level, schema.items || {});
  }
  if (type === 'object') {
    transObjct(data, pid, level, schema || {});
  }
  return result;
};

/**
 *transform list to json<key, value>
 *
 */
export const transListToJson = (list, type, pid = -1) => {
  const child = list.filter(item => item.pid === pid);
  let result;
  if (type === 'object') {
    result = {};
    child.forEach(item => {
      if (item.name) {
        if (item.type === 'array') {
          result[item.name] = transListToJson(list, item.type, item.id);
        } else if (item.type === 'object') {
          result[item.name] = transListToJson(list, item.type, item.id);
        } else {
          if (item.type === 'integer' && /^-?\d+(\.?\d+)?$/.test(item[valueKey]) && item[valueKey] <= 9007199254740992) {
            item[valueKey] = Number(item[valueKey]);
          }
          if (item.type === 'number' && /^-?\d+$/.test(item[valueKey]) && item[valueKey] <= 9007199254740992) {
            item[valueKey] = Number(item[valueKey]);
          }
          if (item.type === 'boolean' && (item[valueKey] === 'true' || item[valueKey] === 'false')) {
            item[valueKey] = JSON.parse(item[valueKey]);
          }
          result[item.name] = item[valueKey];
        }
      }
    });
  } else if (type === 'array') {
    result = [];
    child.forEach(item => {
      if (item.type === 'array') {
        result.push(transListToJson(list, item.type, item.id));
      } else if (item.type === 'object') {
        result.push(transListToJson(list, item.type, item.id));
      } else {
        if (item.type === 'integer' && /^-?\d+(\.?\d+)?$/.test(item[valueKey])) {
          item[valueKey] = Number(item[valueKey]);
        }
        if (item.type === 'number' && /^-?\d+$/.test(item[valueKey])) {
          item[valueKey] = Number(item[valueKey]);
        }
        if (item.type === 'boolean' && (item[valueKey] === 'true' || item[valueKey] === 'false')) {
          item[valueKey] = JSON.parse(item[valueKey]);
        }
        result.push(item[valueKey]);
      }
    });
  }
  return result;
};

/**
 *transform list to schema<key, value>
 *
 */
export const transListToschema = (list, type, pid = -1) => {
  const childs = list.filter(item => item.pid === pid);
  const child = childs[0];
  let result: Record<string, any> = {};
  if (type === 'object') {
    const current = list.find(item => item.id === pid);
    // if (current?.name === 'id') {
    // }
    if (current) {
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const { type, id, name, pid, level, idLine, checked, ...other } = current;
      result = {
        ...other
      };
    }
    result.type = 'object';
    result.properties = {};

    (childs || []).forEach(item => {
      if (item.name) {
        if (item.type === 'object') {
          result.properties[item.name] = transListToschema(list, item.type, item.id);
        } else if (item.type === 'array') {
          result.properties[item.name] = transListToschema(list, item.type, item.id);
        } else {
          // eslint-disable-next-line @typescript-eslint/no-unused-vars
          const { type, id, name, pid, level, idLine, checked, ...other } = item;
          result.properties[item.name] = {
            ...other,
            type: item.type,
            [valueKey]: item[valueKey]
          };
        }
      }
    });
    result[valueKey] = transListToJson(list, type, pid);
  } else if (type === 'array') {
    if (child) {
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const { type, id, name, pid, level, idLine, checked, ...other } = child;
      result = {
        ...other
      };
    }
    result.type = 'array';
    const targetId = ['array', 'object'].includes(child?.type) ? child.id : child?.pid;
    if (targetId) {
      result.items = transListToschema(list, child.type, targetId);
    }
    result[valueKey] = transListToJson(list, type, pid);
  } else {
  // type: string;
  // [valueKey]: string;
  // id: string;
  // name?: string;
  // pid?: string;
  // level: number;
  // idLine: string[];
  // $ref?: string;
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const { type, id, name, pid, level, idLine, checked, ...other } = child;
    result = {
      ...other
    };
    result.type = type;
    result[valueKey] = child[valueKey];
    // {
    //   type: child.type,
    //   [valueKey]: child[valueKey]
    // };
  }
  return result;
};
