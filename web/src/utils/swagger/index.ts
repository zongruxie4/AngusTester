import SwaggerUI from 'swagger-ui';

import { IConfig } from './PropsType';

/**
 * @description 根据openapi v3.x规范解析为application/xml格式
 * @param schema openapi schema
 * @param config SwaggerUI config
 * @returns string
 */
const createXMLExample = (schema: Record<string, any>, config?: IConfig): string => {
  return SwaggerUI.plugins.SamplesFn.createXMLExample(schema, config);
};

/**
 * @description 根据openapi v3.x规范解析为application/json格式
 * @param schema openapi schema
 * @param config SwaggerUI config
 * @returns Record<string, any> | Record<string, any>[]
 */
const sampleFromSchema = (schema: Record<string, any>, config?: IConfig): Record<string, any> | Record<string, any>[] => {
  return SwaggerUI.plugins.SamplesFn.sampleFromSchema(schema, config);
};

const deconstruct = (data: Record<string, any>) => {
  const handler = (schema: Record<string, any>) => {
    if (Object.prototype.hasOwnProperty.call(schema, '$ref')) {
      const model = resolvedRefModels[schema.$ref];
      return handler(model);
    } else {
      for (const key in schema) {
        if (Object.prototype.toString.call(schema[key]) === '[object Object]') {
          schema[key] = handler(schema[key]);
        }
      }
    }

    return schema;
  };

  const model = JSON.parse(data.model || 'null');
  if (model?.$ref === data.ref) {
    return {};
  }
  let resolvedRefModels = data.resolvedRefModels;
  if (!resolvedRefModels) {
    return model;
  }

  resolvedRefModels = Object.entries(resolvedRefModels).reduce((prev, [key, value]) => {
    const _value = (value || 'null') as string;
    prev[key] = JSON.parse(_value);
    return prev;
  }, {});

  return handler(model);
};

export {
  deconstruct,
  createXMLExample,
  sampleFromSchema
};
